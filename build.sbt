name         := "sssn-generator"
version      := "1.0"
scalaVersion := "3.1.3"

enablePlugins(JavaAppPackaging)

val zioAWSVersion = "5.17.233.1"

libraryDependencies ++= Seq(
  "dev.zio"                  %% "zio"            % "2.0.0",
  "dev.zio"                  %% "zio-aws-core"   % zioAWSVersion,
  "dev.zio"                  %% "zio-aws-s3"     % zioAWSVersion,
  "dev.zio"                  %% "zio-aws-netty"  % zioAWSVersion,
  "com.github.mjakubowski84" %% "parquet4s-core" % "2.6.0",
  "org.apache.hadoop"         % "hadoop-client"  % "3.2.1",
  "org.xerial.snappy"         % "snappy-java"    % "1.1.8.4"
)

enablePlugins(DockerPlugin)
maintainer in Docker := "Kurt Fehlhauer"
packageDescription   := "sssn-generator"
