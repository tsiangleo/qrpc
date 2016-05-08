package com.qrpc.proto;

/**
 * 定义了一些常量
 * @author tsiangleo 2016年4月28日 下午6:52:20
 */
public class Defs {
	
	/**
	 * 请求头类型字段的取值
	 * RequestHeader.type的取值类型
	 * @author tsiangleo 2016年4月28日 下午6:53:30
	 */
	public interface ReqTypeCode {
        public final int notification = 0;

        public final int create = 1;

        public final int delete = 2;

        public final int exists = 3;

        public final int getData = 4;

        public final int setData = 5;

        public final int getACL = 6;

        public final int setACL = 7;

        public final int getChildren = 8;

        public final int sync = 9;

        public final int ping = 11;

        public final int getChildren2 = 12;

        public final int check = 13;

        public final int multi = 14;

        public final int auth = 100;

        public final int setWatches = 101;

        public final int sasl = 102;

        public final int createSession = -10;

        public final int closeSession = -11;

        public final int error = -1;
    }
	
	/**
	 * 响应头错误字段的取值
	 * ResponseHeader.err的取值类型
	 * @author tsiangleo 2016年4月28日 下午6:55:30
	 */
	public interface RspErrCode {
		public final int ok = 0;
		public final int error = 1;
	}
}
