package com.example.butterknife_interface;



/**
 * @author : Administrator
 * @time : 17:23
 * @for :
 */
public class ButterKnife {
    public static void bind(Object activity){
        String fullName = activity.getClass().getCanonicalName();
        try {
            Class<?> clz = Class.forName(fullName+"$ViewBinder");
            ViewBinder viewBinder = (ViewBinder) clz.newInstance();
            viewBinder.bind(activity);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}
