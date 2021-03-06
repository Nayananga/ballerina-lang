// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerinax/java;

# Represents network byte order.
#
# BIG_ENDIAN - specifies the bytes to be in the order of most significant byte first
#
# LITTLE_ENDIAN - specifies the byte order to be the least significant byte first
public type ByteOrder "BE"|"LE";

# Specifies the bytes to be in the order of most significant byte first.
public const BIG_ENDIAN = "BE";

# Specifies the byte order to be the least significant byte first.
public const LITTLE_ENDIAN = "LE";

# Represents a WritableDataChannel for writing data.
public type WritableDataChannel object {

    # Initializes data channel.
    #
    # + byteChannel - channel which would represent the source to read/write data
    # + bOrder - network byte order
    public function __init(WritableByteChannel byteChannel, public ByteOrder bOrder = "BE") {
        // Remove temp once this got fixed #19842
        string temp = bOrder;
        initWritableDataChannel(self, byteChannel, java:fromString(temp));
    }

    # Writes 16 bit integer.
    #
    # + value - integer which will be written
    # + return - nill if the content is written successfully or `Error` if any error occurred
    public function writeInt16(int value) returns Error? {
        return writeInt16Extern(self, value);
    }

    # Writes 32 bit integer.
    #
    # + value - integer which will be written
    # + return - nill if the content is written successfully or `Error` if any error occurred
    public function writeInt32(int value) returns Error? {
        return writeInt32Extern(self, value);
    }

    # Writes 64 bit integer.
    #
    # + value - integer which will be written
    # + return - nill if the content is written successfully or `Error` if any error occurred
    public function writeInt64(int value) returns Error? {
        return writeInt64Extern(self, value);
    }

    # Writes 32 bit float.
    #
    # + value - float which will be written
    # + return - nill if the float is written successfully or `Error` if any error occurred
    public function writeFloat32(float value) returns Error? {
        return writeFloat32Extern(self, value);
    }

    # Writes 64 bit float.
    #
    # + value - float which will be written
    # + return - nill if the float is written successfully or `Error` if any error occurred
    public function writeFloat64(float value) returns Error? {
        return writeFloat64Extern(self, value);
    }

    # Writes boolean.
    #
    # + value - boolean which will be written
    # + return - nill if the content is written successfully or `Error` if any error occurred
    public function writeBool(boolean value) returns Error? {
        return writeBoolExtern(self, value);
    }

    # Writes a given string value to the respective channel.
    #
    # + value - the value which should be written
    # + encoding - the encoding which will represent the value string
    # + return - nill if the content is written successfully or `Error` if any error occurred
    public function writeString(string value, string encoding) returns Error? {
        return writeStringExtern(self, java:fromString(value), java:fromString(encoding));
    }

    # Writes a variable length integer.
    #
    # + value - int which will be written
    # + return - value of the integer which is read or `Error` if any error occurred
    public function writeVarInt(int value) returns Error? {
        return writeVarIntExtern(self, value);
    }

    # Closes the data channel.
    #
    # + return - nill if the channel is closed successfully or `Error` if any error occurred
    public function close() returns Error? {
        return closeWritableDataChannelExtern(self);
    }
};

function initWritableDataChannel(WritableDataChannel dataChannel, WritableByteChannel byteChannel, handle bOrder) = @java:Method {
    name: "initWritableDataChannel",
    class: "org.ballerinalang.stdlib.io.nativeimpl.DataChannelUtils"
} external;

function writeInt16Extern(WritableDataChannel dataChannel, int value) returns Error? = @java:Method {
    name: "writeInt16",
    class: "org.ballerinalang.stdlib.io.nativeimpl.DataChannelUtils"
} external;

function writeInt32Extern(WritableDataChannel dataChannel, int value) returns Error? = @java:Method {
    name: "writeInt32",
    class: "org.ballerinalang.stdlib.io.nativeimpl.DataChannelUtils"
} external;

function writeInt64Extern(WritableDataChannel dataChannel, int value) returns Error? = @java:Method {
    name: "writeInt64",
    class: "org.ballerinalang.stdlib.io.nativeimpl.DataChannelUtils"
} external;

function writeFloat32Extern(WritableDataChannel dataChannel, float value) returns Error? = @java:Method {
    name: "writeFloat32",
    class: "org.ballerinalang.stdlib.io.nativeimpl.DataChannelUtils"
} external;

function writeFloat64Extern(WritableDataChannel dataChannel, float value) returns Error? = @java:Method {
    name: "writeFloat64",
    class: "org.ballerinalang.stdlib.io.nativeimpl.DataChannelUtils"
} external;

function writeBoolExtern(WritableDataChannel dataChannel, boolean value) returns Error? = @java:Method {
    name: "writeBool",
    class: "org.ballerinalang.stdlib.io.nativeimpl.DataChannelUtils"
} external;

function writeStringExtern(WritableDataChannel dataChannel, handle value, handle encoding) returns Error? = @java:Method {
    name: "writeString",
    class: "org.ballerinalang.stdlib.io.nativeimpl.DataChannelUtils"
} external;

function writeVarIntExtern(WritableDataChannel dataChannel, int value) returns Error? = @java:Method {
    name: "writeVarInt",
    class: "org.ballerinalang.stdlib.io.nativeimpl.DataChannelUtils"
} external;

function closeWritableDataChannelExtern(WritableDataChannel dataChannel) returns Error? = @java:Method {
    name: "closeDataChannel",
    class: "org.ballerinalang.stdlib.io.nativeimpl.DataChannelUtils"
} external;
