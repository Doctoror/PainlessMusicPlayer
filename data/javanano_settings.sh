#!/bin/bash
protoc ./src/main/proto/settings_proto.proto --javanano_out=./src/main/java/ --proto_path=./src/main/proto/
