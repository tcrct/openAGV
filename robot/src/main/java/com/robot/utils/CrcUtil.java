package com.robot.utils;

public class CrcUtil {

    /**
     * 获取源数据和验证码的组合byte数组
     *
     * @param strings 可变长度的十六进制字符串
     * @return
     */
    public static byte[] appendCrc16(String... strings) {
        byte[] data = new byte[]{};
        for (int i = 0; i < strings.length; i++) {
            int x = Integer.parseInt(strings[i], 16);
            byte n = (byte) x;
            byte[] buffer = new byte[data.length + 1];
            byte[] aa = {n};
            System.arraycopy(data, 0, buffer, 0, data.length);
            System.arraycopy(aa, 0, buffer, data.length, aa.length);
            data = buffer;
        }
        return appendCrc16(data);
    }

    /**
     * 获取源数据和验证码的组合byte数组
     *
     * @param aa 字节数组
     * @return
     */
    public static byte[] appendCrc16(byte[] aa) {
        byte[] bb = getCrc16(aa);
        byte[] cc = new byte[aa.length + bb.length];
        System.arraycopy(aa, 0, cc, 0, aa.length);
        System.arraycopy(bb, 0, cc, aa.length, bb.length);
        return cc;
    }

    public static int CrcVerify(String sourceStr) {
        int i, j;
        int wCrc = 0xffff;
        int wPolynom = 0xA001;
        char[] cBuffer = sourceStr.toCharArray();
        int iBufLen = cBuffer.length;

        for (i = 0; i < iBufLen; i++) {
            wCrc ^= cBuffer[i];
            for (j = 0; j < 8; j++) {
                if ((wCrc & 0x0001) > 0) {
                    wCrc = (wCrc >> 1) ^ wPolynom;
                } else {
                    wCrc = wCrc >> 1;
                }
            }
        }
        return wCrc;
    }

    public static String CrcVerify_Str(String sourceStr) {
        return toString("%04x", CrcVerify(sourceStr));
    }

    /**
     * 获取验证码byte数组，基于Modbus CRC16的校验算法
     */
    public static byte[] getCrc16(byte[] arr_buff) {
        int len = arr_buff.length;

        // 预置 1 个 16 位的寄存器为十六进制FFFF, 称此寄存器为 CRC寄存器。
        int crc = 0xFFFF;
        int i, j;
        for (i = 0; i < len; i++) {
            // 把第一个 8 位二进制数据 与 16 位的 CRC寄存器的低 8 位相异或, 把结果放于 CRC寄存器
            crc = ((crc & 0xFF00) | (crc & 0x00FF) ^ (arr_buff[i] & 0xFF));
            for (j = 0; j < 8; j++) {
                // 把 CRC 寄存器的内容右移一位( 朝低位)用 0 填补最高位, 并检查右移后的移出位
                if ((crc & 0x0001) > 0) {
                    // 如果移出位为 1, CRC寄存器与多项式A001进行异或
                    crc = crc >> 1;
                    crc = crc ^ 0xA001;
                } else
                    // 如果移出位为 0,再次右移一位
                    crc = crc >> 1;
            }
        }
        return intToBytes(crc);
    }

    /**
     * 将int转换成byte数组，低位在前，高位在后
     * 改变高低位顺序只需调换数组序号
     */
    private static byte[] intToBytes(int value) {
        byte[] src = new byte[2];
        src[1] = (byte) ((value >> 8) & 0xFF);
        src[0] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * int --> byte[] 整形转byte[]
     *
     * @param res
     * @return
     */
    public static byte[] int2byte(int res) {
        byte[] targets = new byte[4];

        targets[0] = (byte) (res & 0xff);// 最低位
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低位
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高位
        targets[3] = (byte) (res >>> 24);// 最高位,无符号右移。
        return targets;
    }

    /**
     * byte[] -->int byte[]转整形
     *
     * @param res
     * @return
     */
    public static int byte2int(byte[] res) {
        // 一个byte数据左移24位变成0x??000000，再右移8位变成0x00??0000

        int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00) // | 表示安位或
                | ((res[2] << 24) >>> 8) | (res[3] << 24);
        return targets;
    }

    private static String toString(String format, int crcInt) {
//        String format = "%04x"
        try {
            return String.format(format, crcInt);
        } catch (Exception e) {
            return "";
        }
    }

    public static void main(String[] args) {
        String aa = "##,,A006,,r,,setrout,,mf705::mf706::sf702,,";
        System.out.println(CrcVerify_Str(aa));
    }
}
