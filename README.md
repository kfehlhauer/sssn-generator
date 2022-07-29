# sssn-generator (AKA. Synthetic SSN generator)

Sample app demonstrating the creation of synthetic mapping of PII (Personal Identifying Information) using U.S. Social Security Numbers for context. The sample data lools like:
```
ssn,birth_date
100-10-1000,1900-01-01
100-10-1001,1900-01-02
100-10-1002,1900-01-03
111-11-1111,2000-12-24
222-22-2222,2000-12-31
333-33-3333,2001-05-31
444-44-4444,2008-07-04
555-55-5555,2003-05-05
777-77-7777,2017-04-23
888-88-8888,2022-01-01
```
The app will generate a parquet file containing the original SSN, a syntetic SSN using random UUID, and the birtdate.

This app uses the [ZIO framework](https://zio.dev) and demonstrates the following:
1. Reading a CSV file from AWS S3 and parse the file using ZIO ZStreams.
2. Parsing a CSV file using a ZIO ZStream.
3. Saving the results back using a Snappy compressed Parquet file.
