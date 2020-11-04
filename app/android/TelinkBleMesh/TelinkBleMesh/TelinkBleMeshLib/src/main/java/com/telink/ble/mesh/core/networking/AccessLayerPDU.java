package com.telink.ble.mesh.core.networking;

/**
 * access payload
 * Created by kee on 2019/7/29.
 */

import com.telink.ble.mesh.core.MeshUtils;
import com.telink.ble.mesh.core.message.OpcodeType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * An access payload may be sent in up to 32 segments of 12 octets each.
 * This implies that the maximum number of octets is 384 including the TransMIC
 * <p>
 * 12 * 32
 * packet-len * packet-cnt
 * <p>
 * transMIC 4 bytes
 * at least 1 byte opcode
 */
public class AccessLayerPDU implements NetworkingPDU {
    // 1, 2 or 3 bytes
    public int opcode;

    // 0 ~ 379 bytes
    public byte[] params;

//    public byte[] decryptedPayload;

    private AccessLayerPDU() {
    }


    public AccessLayerPDU(int opcode, byte[] params) {
        this.opcode = opcode;
        this.params = params;
    }


    public static AccessLayerPDU parse(byte[] payload) {
        AccessLayerPDU accessPDU = new AccessLayerPDU();
        OpcodeType opType = OpcodeType.getByFirstByte(payload[0]);

        accessPDU.opcode = 0;
        int index = 0;
        for (int i = 0; i < opType.length; i++) {
            accessPDU.opcode |= (payload[index++] & 0xFF) << (8 * i);
        }

        final int paramLen = payload.length - opType.length;
        accessPDU.params = new byte[paramLen];
        System.arraycopy(payload, index, accessPDU.params, 0, paramLen);
        return accessPDU;
    }


    @Override
    public byte[] toByteArray() {
        int opcodeLen = OpcodeType.getByFirstByte((byte) opcode).length;
        if (params == null || params.length == 0) {
            return MeshUtils.integer2Bytes(opcode, opcodeLen, ByteOrder.LITTLE_ENDIAN);
        } else {
            return ByteBuffer.allocate(opcodeLen + params.length).order(ByteOrder.LITTLE_ENDIAN)
                    .put(MeshUtils.integer2Bytes(opcode, opcodeLen, ByteOrder.LITTLE_ENDIAN))
                    .put(params)
                    .array();
        }
    }
}