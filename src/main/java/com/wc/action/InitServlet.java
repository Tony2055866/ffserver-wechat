package com.wc.action;

import com.wc.dao.UserLangDao;
import com.wc.dao.WcUserDAO;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Created by Administrator on 2015/3/3.
 */
public class InitServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        super.init();
//        Logg
        System.out.println("InitServlet started");
        final WcUserDAO userDAO = new WcUserDAO();
        new Thread(){
            @Override
            public void run() {
                while(true){
                    userDAO.findById("11");
                    try {
                        Thread.sleep(120*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }.start();
    }
}
