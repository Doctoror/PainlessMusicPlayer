#!/bin/bash
protoc ./src/main/proto/persistable_playlist.proto --javanano_out=./src/main/java/ --proto_path=./src/main/proto/
