package test;

import com.wc.bean.WcUser;
import com.wc.dao.WcUserDAO;
import com.wc.tools.Blowfish;

public class UserDaoTest {
	public static void main(String[] args) {
		/* WcUserDAO uDao = new WcUserDAO();
		WcUser u = uDao.findById("402880814b68c502014b68c507d40000");
		System.out.println(u.getFriends());*/
		
		Blowfish b = new Blowfish("fj0f041A5l3Kl5j");
		String p = b.encryptString("admin");
		System.out.println(p);
		
		System.out.println(b.decryptString("608830e8d220d9a67434fd6870b8d9f717856d5df40213f0"));
	}
}
