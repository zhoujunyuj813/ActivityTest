package com.example.wbdemo.info.maindata;

import java.io.Serializable;

/**
 * Created by zhoujunyu on 2019/5/23.
 */
public class AnnotationsBean implements Serializable {

        /**
         * mapi_request : true
         */

        private boolean mapi_request;

        public boolean isMapi_request() {
            return mapi_request;
        }

        public void setMapi_request(boolean mapi_request) {
            this.mapi_request = mapi_request;
        }

}
