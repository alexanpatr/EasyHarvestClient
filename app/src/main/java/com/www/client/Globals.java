package com.www.client;

import android.os.Environment;

public class Globals {
    /**
     * Evaluation
     */
//    public static boolean eval_status = true;
     public static boolean eval_status = false;

    // Beauty
//    public static String eval_dev = "MID7188";
//    public static String eval_ip = "192.168.49.1";

    // Nexus
//    public static String eval_dev = "occam";
//    public static String eval_ip = "192.168.49.75";

    /**
     * Description: device URLS.
     * Changelog  :  - .
     */
    public static String sdcard_dir = Environment.getExternalStorageDirectory().getPath();
    public static String client_dir = sdcard_dir + "/Client";
    public static String pms_dir = client_dir + "/PMs";

    /**
     * Description: URLS.
     * Changelog  :  - .
     */
    public static String server_url = "http://83.212.109.118:8080/Server/webresources";
//    public static String server_url = "http://192.168.2.2:8084/Server/webresources";
//    public static String server_url = "http://10.64.83.194:8084/Server/webresources";

    public static String devs_url = server_url + "/devs";
    public static String tasks_url = server_url + "/tasks";

    public static String pms_url = server_url + "/pms";
    public static int pm_port = 4545;

    /**
     * Description: Time latency in ms.
     * Changelog  :  - .
	 */
    public static int task_latency = 10 * 100 * 1000;
    public static int pm_latency = 1 * 100 * 1000;

    /**
     * Description: EasyHarvest client shared preferences labels.
     * Changelog  :  - .
     */
    public static String shr_user = "username";         // string
    public static String shr_dev_id = "deviceID";         // string

    /**
     * Description: Sensing tasks shared preferences labels.
     * Changelog  :  - .
	 */
    public static String st_id = "taskID";                // string


    /**
     * Description: Privacy mechanisms shared preferences labels.
     * Changelog  :  - .
	 */
    public static String easy_privacy = "EasyPrivacy";      // boolean

    public static String privacy_level = "privacyLevel";    // integer

    public static String pm_name = "pmName";                // string
    public static String pm_clss = "pmClss";                //
    public static String pm_id = "pmID";                    //
    public static String pm_vers = "pmVersion";             //
    public static String pm_st_id = "pmTaskId";             //
    public static String pm_user = "pmUser";                //
    public static String pm_desc = "pmDescription";         //
    public static String pm_date = "pmDate";                //
    public static String pm_time = "pmTime";                //
    public static String pm_size = "pmSize";                //

}
