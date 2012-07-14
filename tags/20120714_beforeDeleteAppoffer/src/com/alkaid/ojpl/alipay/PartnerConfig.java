package com.alkaid.ojpl.alipay;

public class PartnerConfig {

	// 合作商户ID。用签约支付宝账号登录ms.alipay.com后，在账户信息页面获取。
	public static final String PARTNER = "2088002290856176";
	// 商户收款的支付宝账号
	public static final String SELLER = "xiamao_46@163.com";
	// 商户（RSA）私钥
	public static final String RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMkjNYuWNJfF1BlcXblfuyw4mXfmRMVKxAIjsTKkkdZpjutDpJ28JX31IYESuxC+uW5aSR39Dm/y6W3yasirosXA4ja+FfOVTqe0pJ1yj/EZLXHoHPL6BRqSfB9lYyFwJyMdHG8mlYnGBI5ZXfJHSdlRHeNOgJfsnOnZVdZTAedXAgMBAAECgYEApUTzCGFBkbwRzUziDiGlEG7pW2Wv+FS4vfFJ9ozW8FEICDQqnRktzVOQVhhn8RhmEVDKZ4O5Sy9Tsu3P5FdzhIhKpZC28fazqhoIC6a1PhzuC7ukQ2Gymo8moo8SdTE7z6K9221KduaabHddzntANhHzrrycYSqFHBKc2VWZ/mECQQD0axms+QTAbxyxOUVIaokd0lXL2Zyb4slia2/W1Zts+KaUyR/MBPIWd8s7UVHVCiIvUHeGwQAHSGFz57gXGyaJAkEA0qsYB9wCg7mhmJaDAOzW/sS61i4tbUop2tI9Q4y7qrwB3lrxqsOv4NUcthRIBJJyXsSpNZjoI14uNKZ4vMsm3wJAGKP/d6e3KetJujgq2u0am64LxjxPBIwtf0WThdYNEERVDuTj1r6c5VT4YSeGl3KpFZoIIsasSQ+r+3Jd5b5v+QJAbJuucObZGQYLrHn0Ifb4RDIyTJdT7iMs/tYipX+ZhMUWhYcHTk1CkDvuaGR+WHUTp43l2m7xRsKYOaaVWme1qwJAHNV7M/UtNmNG3JVO0nZcN2P8LKo/1eHigm7VD0HcXOtB7ntsgoXOlYDML36ID9ggvfOSSYvfuFpsyY/JPnL1Hw==";
	// 支付宝（RSA）公钥 用签约支付宝账号登录ms.alipay.com后，在密钥管理页面获取。
	public static final String RSA_ALIPAY_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCW6Ry8c8Qfb0PmBlqn0h4lG214U1T7qt+1cKFy0P53LXhXkbUOJ65fo1K/FWU6Jyx7JpZTT9HLSQWxV3JCRBk/xtamZI48KgVTTFsGnTDhHyx1U/KRJ9Pg8noNhsUd6NT8i7habNoqqYL71pZgu5jo3hnZUKaSl5EtdZd9/cW1fwIDAQAB";
	// 支付宝安全支付服务apk的名称，必须与assets目录下的apk名称一致
	public static final String ALIPAY_PLUGIN_NAME = "alipay_plugin_20120428msp.apk";

}
