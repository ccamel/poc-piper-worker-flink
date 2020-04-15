poc-piper-worker-flink
======================
![Build Status](https://github.com/ccamel/poc-piper-worker-flink/workflows/Build/badge.svg)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/653e27dc026745299c50343bc2b1c3bc)](https://app.codacy.com/app/ccamel/poc-piper-worker-flink?utm_source=github.com&utm_medium=referral&utm_content=ccamel/poc-piper-worker-flink&utm_campaign=Badge_Grade_Dashboard) [![StackShare](https://img.shields.io/badge/tech-stack-0690fa.svg?style=flat)](https://stackshare.io/ccamel/poc-piper-worker-flink)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-brightgreen.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

> An experiment :alembic: to make [flink][] a worker for the workflow engine [piper][].

## Description

[piper] is an open-source, distributed workflow engine designed to be dead simple.

This project demonstrates the great capability of [piper][] to integrate with [flink][], the stream-processing framework from the [Apache Software Foundation](https://en.wikipedia.org/wiki/Apache_Software_Foundation).

The integration is achieved by designing a Flink Data Stream able to communicate with [piper][] through [RabitMQ](https://www.rabbitmq.com/).

![piper-flink-overview](doc/piper-flink-overview.png)

## Build

```sh
sbt clean package
```

[flink]: https://flink.apache.org/
[piper]: https://github.com/creactiviti/piper

[Chris Camel]: https://github.com/ccamel
[MIT]: https://tldrlegal.com/license/mit-license
