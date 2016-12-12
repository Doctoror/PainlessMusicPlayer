#!/bin/bash
protoc ./src/main/proto/recent_playlists.proto --javanano_out=./src/main/java/ --proto_path=./src/main/proto/
