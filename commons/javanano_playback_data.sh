#!/bin/bash
protoc ./src/main/proto/proto_playback_data.proto --javanano_out=./src/main/java/ --proto_path=./src/main/proto/
