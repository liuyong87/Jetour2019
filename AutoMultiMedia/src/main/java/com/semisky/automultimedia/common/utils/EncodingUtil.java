package com.semisky.automultimedia.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EncodingUtil {
    private final String TAG = EncodingUtil.class.getSimpleName();
    private boolean debug = false;

    public String getEncoding(String str) {
        String[] encodes = {"GB2312", "ISO-8859-1", "GB18030", "UTF-8", "GBK", "UTF-16BE", "UTF-16LE"};
        for (String encode : encodes) {
            try {
                if (str.equals(new String(str.getBytes(encode), encode))) {
                    return encode;
                }
            } catch (Exception exception) {
            }
        }
        return null;
    }

    public String getEncodeString(String source, String encode) {
        if (encode == null) {
            encode = "GB2312";
        }

        String old_encode = getEncoding(source);
        if (debug)
            LogUtil.d(TAG, "old_encode: " + old_encode);
        if (encode.equals(old_encode) || "GB18030".equals(old_encode)) {
            if (debug)
                LogUtil.d(TAG, "return source: " + source);
            return source;
        }
        try {
            return new String(source.getBytes(old_encode), encode);
        } catch (Exception e) {
            return source;
        }
    }

    /**
     *
     * 是否是中文
     * @param txt
     * @return
     */
    public static boolean isChinese(String txt) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        for (int i = 0; i < txt.length(); i++) {
            String ts = txt.charAt(i) + "";
            Matcher m = p.matcher(ts);
            if (!m.matches()) {
                return false;
            }
        }
        return true;
    }

    /**
     *字符只能包含是字母，数字
     */
    public static boolean isLetterAndNumber(String txt) {
        final String REGEX = "^[0-9a-zA-Z]$";
        if (Pattern.matches(REGEX, txt)) {
            return true;
        }
        return false;
    }
    public static boolean isNormalText(String string){
        final String REGEX = "^[\\u4e00-\\u9fa5_().a-zA-Z0-9]+$";
        if (Pattern.matches(REGEX, string)) {
            return true;
        }
        return false;
    }


}
