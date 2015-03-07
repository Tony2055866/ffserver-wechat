package com.wc.action;

import java.io.File;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.FormDataParam;
import com.wc.bean.OfUser;
import com.wc.bean.UserLang;
import com.wc.bean.WcFile;
import com.wc.bean.WcLoginInfo;
import com.wc.bean.WcUser;
import com.wc.dao.OfUserDAO;
import com.wc.dao.UserLangDao;
import com.wc.dao.WcFileDAO;
import com.wc.dao.WcLoginInfoDAO;
import com.wc.dao.WcUserDAO;
import com.wc.tools.Blowfish;
import com.wc.tools.FileUtil;
import com.wc.tools.SimpleJSONArray;
import com.wc.tools.SimpleJSONObject;
import com.wc.tools.StringUtil;

@Produces("application/json;charset=UTF-8")
// @Consumes("charset=UTF-8")
@Path("/v2/")
public class MainV2Actions {

	private static Logger logger = LoggerFactory.getLogger(MainV2Actions.class);
	//常量
	public static final String androidVersion="1.0.0";
	public static final String iosVersion="4.0.0";
	
	/* dao组 */
	private WcUserDAO uDao = new WcUserDAO();
	private OfUserDAO ofDAO=new OfUserDAO();
	private WcFileDAO fileDao=new WcFileDAO();
	private WcLoginInfoDAO logDao=new WcLoginInfoDAO();

	private SimpleJSONObject res = new SimpleJSONObject();
	private static Blowfish _blow = new Blowfish("weChat4.0");
	private static Blowfish _of = new Blowfish("fj0f041A5l3Kl5j");
	/* 用户模块 */

	/* 注册接口 */
	@GET
	@Path("test.action")
	public String test(@PathParam("name") String name,
			@Context HttpServletRequest request) {
		
		String relativeWebPath = "/index.jsp";
		String absoluteDiskPath = request.getRealPath("/");
		logger.debug("test absoluteDiskPath:" + absoluteDiskPath + "res" + File.separator + "test.txt");
		return "{test}";
	}
	
	/* 登陆接口 */
	//@GET
	@POST
	@Path("login.action")
	public String login(@FormParam("uname") String uname,
			@FormParam("uPass") String uPass,
			@FormParam("versionInfo") String versionInfo,
			@FormParam("deviceInfo") String deviceInfo,
			@Context HttpServletRequest request) {
		WcUser user = null;
		if(uname.contains("@"))
			user = uDao.findByEmail(uname);
		
		if (user == null) {
			logger.info("登陆失败，该用户名不存在，您可以先注册");
			// 注册失败，用户名已存在
			res.add("status", -1);
			res.add("msg", "登陆失败，该用户名不存在，您可以先注册");
			return res.toString();
		}

		if (user.getUserPassword().equalsIgnoreCase(uPass)) {
			user.setApiKey(_blow.encryptString(uname + uPass
					+ System.currentTimeMillis()));
			user = uDao.update(user);
			res.add("status", 1);
			res.add("msg", "登陆成功");
			res.add("apiKey", user.getApiKey());

			SimpleJSONObject userJson = user.toJSON();
			res.add("userInfo", userJson);
			WcLoginInfo log=new WcLoginInfo();
			log.setLoginDevice(deviceInfo);
			log.setLoginResult("登陆成功");
			log.setLoginTime(new Timestamp(System.currentTimeMillis()));
			log.setLoginVersion(versionInfo);
			log.setUserLoginName(uname);
			logDao.save(log);

			logger.info("登陆成功," + uname);
		} else {
			res.add("status", -2);
			res.add("msg", "密码错误");
			logger.info("密码错误," + uname);
		}
		return res.toString();
	}

	/* 注册接口 */
	@POST
	//@GET
	@Path("register.action")
	public String register(@FormParam("name") String name,
			@FormParam("uPass") String uPass,
			@FormParam("email") String email,
			@FormParam("mLang") String mLang,
			@FormParam("lLang") String lLang,
			@Context HttpServletRequest request) {

		/*if (uDao.findByUserName(name).size() != 0) {
			// 注册失败，用户名已存在
			res.add("status", -2);
			res.add("msg", "注册失败，该用户名已注册");
			return res.toString();
		}*/
		
		if (uDao.findByEmail(email) != null) {
			// 注册失败，用户名已存在
			res.add("status", -2);
			res.add("msg", "注册失败，该邮箱已注册");
			return res.toString();
		}

		//WcFile file=fileDao.findById(userHead);
		WcUser user = new WcUser();
		user.setUserNickname(name);
		user.setUserPassword(uPass);
		user.setEmail(email);
		
		user.setmLang(mLang);
		user.setlLang(lLang);
		
		user.setApiKey(_blow.encryptString(name + uPass
				+ System.currentTimeMillis()));
		
		logger.info("save user:" + user);
		uDao.save(user);

		//更新用户的母语列表，用于匹配用户
		updateUserMLangList(mLang, user);

		registerOpenFireUser(user);
		res.add("status", 1);
		res.add("msg", "注册成功");
		res.add("apiKey", user.getApiKey());
		res.add("userInfo", user.toJSON());
		return res.toString();
	}

	public static void updateUserMLangList(String mLang, WcUser user) {
		//先删除原有的
		EntityManager manager = UserLangDao.getEntityManager();
		manager.getTransaction().begin();
		Query query = manager.createNativeQuery("delete from user_lang where userId=?");
		query.setParameter(1, mLang);
		query.executeUpdate();
		manager.getTransaction().commit();

		//插入新的
		UserLangDao dao = new UserLangDao();
		if(mLang !=null && !mLang.trim().equals("")){
			String ids[] = mLang.split(",");
			for(String id:ids){
				UserLang userlang = new UserLang();
				userlang.setLangId(Integer.parseInt(id));
				userlang.setUserId(user.getUserId());
				dao.save(userlang);
			}
		}
	}

	/*
	 * 注册openFire
	 * */
	private boolean registerOpenFireUser(WcUser user) {
		// TODO Auto-generated method stub
		String encodedStr=_of.encryptString(user.getUserPassword());
		OfUser openFireUser=new OfUser(user.getUserId(), user.getUserPassword(), encodedStr, user.getUserNickname(), user.getEmail(), String.format("00%d", System.currentTimeMillis()), String.format("00%d", System.currentTimeMillis()));
		ofDAO.save(openFireUser);
		return true;
	}
	public static void main(String[] args) throws InterruptedException {
		String str = _of.encryptString("nihaolll");
		System.out.println(str);;
		Thread.sleep(2000);
		System.out.println(_of.decryptString(str));
	}

}