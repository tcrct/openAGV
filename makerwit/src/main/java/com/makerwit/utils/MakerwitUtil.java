package com.makerwit.utils;

import cn.hutool.core.util.StrUtil;
import com.makerwit.numes.MakerwitEnum;
import com.openagv.mvc.core.exceptions.AgvException;
import com.openagv.mvc.utils.SettingUtils;
import com.openagv.mvc.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 工具
 * Created by laotang on 2020/1/13.
 */
public class MakerwitUtil {

    private static final Logger LOG =  LoggerFactory.getLogger(MakerwitUtil.class);

    private static Set<String> DEVICE_FLAG_SET;

    /**
     * 是否允许指定的设备或车辆访问
     * @param deviceId 设备ID或车辆ID
     * @return 允许访问返回true
     */
    public static boolean isAllowAccess(String deviceId) {
        return getDeviceFlagSet().contains(deviceId);
    }

    /**
     * 取配置文件里设置的设备ID
     * */
    private static Set<String> getDeviceFlagSet() {
        if(null == DEVICE_FLAG_SET) {
            DEVICE_FLAG_SET = SettingUtils.getStringsToSet("device.name", "security");
            if (ToolsKit.isEmpty(DEVICE_FLAG_SET)) {
                throw new AgvException("请先在app.setting里设置device.name值！该值用于允许指定的车辆或设备ID访问系统");
            }
        }
        return DEVICE_FLAG_SET;
    }

    /**
     * 将接收到的报文按规则拆分成报文List组合
     * @param telegram 报文
     * @return telegram 报文List集合
     */
    public static List<String> getTelegram2List(String telegram) {
//        String telegram = "##,,A002,,s,,setrout,,mf400::mf708,,721a,,ZZ##,,A002,,s,,setrout,,mf400::mf708,,721b,,ZZ##,,A002,,s,,setrout,,mf400::mf708,,721c,,ZZ";

//        A030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZA030##,,A030,,s,,rpterr,,0040,,d229,,ZZA030##,,A030,,s,,rptmag,,0::0::1::1,,ff8a,,ZZ

        List<String> returnTelegramList = null;
        String splitString = MakerwitEnum.SEPARATOR.getValue() + MakerwitEnum.FRAME_END.getValue();
        List<String> telegramList = StrUtil.splitTrim(telegram, splitString);
        if (ToolsKit.isNotEmpty(telegramList)) {
            returnTelegramList = new ArrayList<>(telegramList.size());
            for (String telegramItem : telegramList) {
                String telegramData = telegramItem + splitString;
                if(!ProtocolUtil.checkTelegramFormat(telegramData)) {
                    LOG.info("报文["+telegramData+"]格式不正确");
                    continue;
                }
                returnTelegramList.add(telegramData);
            }
        }
        return returnTelegramList;
    }
}
