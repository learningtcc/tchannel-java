/*
 * Copyright (c) 2015 Uber Technologies, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.uber.tchannel.messages;

import com.uber.tchannel.codecs.CodecUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;

import java.util.Map;

public class ThriftSerializer implements Serializer.SerializerInterface {
    @Override
    public String decodeEndpoint(ByteBuf arg1) {
        String endpoint = arg1.toString(CharsetUtil.UTF_8);
        return endpoint;
    }

    @Override
    public Map<String, String> decodeHeaders(ByteBuf arg2) {
        Map<String, String> headers = CodecUtils.decodeHeaders(arg2);
        return headers;
    }

    @Override
    public <T> T decodeBody(ByteBuf arg3, Class<T> bodyType) {

        try {
            // Create a new instance of type 'T'
            T base = bodyType.newInstance();

            // Get byte[] from ByteBuf
            byte[] payloadBytes = new byte[arg3.readableBytes()];
            arg3.readBytes(payloadBytes);

            // Actually deserialize the payload
            TDeserializer deserializer = new TDeserializer(new TBinaryProtocol.Factory());
            deserializer.deserialize((TBase) base, payloadBytes);

            return base;
        } catch (InstantiationException | IllegalAccessException | TException e) {
            e.printStackTrace();
        }

        return null;

    }

    @Override
    public ByteBuf encodeEndpoint(String method) {
        return Unpooled.wrappedBuffer(method.getBytes());
    }

    @Override
    public ByteBuf encodeHeaders(Map<String, String> applicationHeaders) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        CodecUtils.encodeHeaders(applicationHeaders, buf);
        return buf;
    }

    @Override
    public ByteBuf encodeBody(Object body) {
        try {
            TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
            byte[] payloadBytes = serializer.serialize((TBase) body);
            return Unpooled.wrappedBuffer(payloadBytes);
        } catch (TException e) {
            e.printStackTrace();
        }
        return null;
    }
}
