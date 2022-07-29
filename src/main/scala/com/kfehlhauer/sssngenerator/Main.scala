package com.kurtfehlhauer.sssngenerator

import zio.*
import zio.Console.*
import zio.Clock.*
import zio.stream.{ZPipeline, ZSink}
import zio.aws.core.config.AwsConfig
import zio.aws.netty
import zio.aws.s3.model.*
import zio.aws.s3.model.primitives.*
import zio.aws.s3.S3
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.time.Instant
import com.github.mjakubowski84.parquet4s.{ParquetWriter, Path}
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import org.apache.hadoop.conf.Configuration
import java.sql.Date

final case class SSSN(
  ss: String,
  sssn: String,
  date: Date
)

object Main extends zio.ZIOAppDefault:
  val read =
    for
      getResponse <- S3.getObject(
                       GetObjectRequest(
                         bucket = BucketName("example-bucket"),
                         key = ObjectKey("raw/social_security_numbers.csv")
                       )
                     )
    yield getResponse.output

  val sink = ZSink.collectAll[SSSN]

  val ssn2Sssn =
    ZPipeline.map[String, SSSN] { x =>
      val ssnBirthDate = x.split(",")

      SSSN(ssnBirthDate(0), UUID.randomUUID.toString, Date.valueOf(ssnBirthDate(1)))
    }

  val hadoopConf = new Configuration()
  hadoopConf.set("fs.s3a.path.style.access", "true")

  val writerOptions = ParquetWriter.Options(
    compressionCodecName = CompressionCodecName.SNAPPY,
    hadoopConf = hadoopConf
  )

  def write(sssns: Chunk[SSSN]) = ZIO.attemptBlocking(
    ParquetWriter
      .of[SSSN]
      .options(writerOptions)
      .writeAndClose(
        Path(
          "s3a://example-bucket/refined/synthetic_ssn_lookup.snappy.parquet"
        ),
        sssns
      )
  )

  val program =
    for
      ssns <- read
      syntheticSsns <-
        ssns
          .via(
            ZPipeline.drop(13) >>> ZPipeline.utf8Decode >>> ZPipeline.splitLines >>> ssn2Sssn
          )
          .run(sink)
      _ <- write(syntheticSsns)
    yield ()

  def run =
    val s3Client =
      S3.customized(
        _.credentialsProvider(
          DefaultCredentialsProvider
            .builder()
            .build()
        ).region(Region.US_EAST_1)
      )

    val httpClient = netty.NettyHttpClient.default
    val awsConfig  = httpClient >>> AwsConfig.default
    val aws        = awsConfig >>> (S3.live >>> s3Client)

    program.provide(aws)
