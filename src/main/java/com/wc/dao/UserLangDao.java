package com.wc.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.wc.bean.OfUser;
import com.wc.bean.UserLang;
import com.wc.jpa.EntityManagerHelper;

public class UserLangDao {

	public static EntityManager getEntityManager() {
		return EntityManagerHelper.getEntityManager();
	}

	public void save(UserLang entity) {
		EntityManagerHelper.log("saving UserLang instance", Level.INFO, null);
		try {
			getEntityManager().getTransaction().begin();
			getEntityManager().persist(entity);
			getEntityManager().getTransaction().commit();
			EntityManagerHelper.log("save successful", Level.INFO, null);
		} catch (RuntimeException re) {
			EntityManagerHelper.log("save failed", Level.SEVERE, re);
			throw re;
		}
	}
	
	public List<UserLang> findByProperty(String propertyName, final Object value
	        , final int...rowStartIdxAndCount
	        ) {
	    				EntityManagerHelper.log("finding UserLang instance with property: " + propertyName + ", value: " + value, Level.INFO, null);
				try {
				final String queryString = "select model from UserLang model where model." 
				 						+ propertyName + " in :propertyValue";
									Query query = getEntityManager().createQuery(queryString);
						query.setParameter("propertyValue", value);
						if (rowStartIdxAndCount != null && rowStartIdxAndCount.length > 0) {	
							int rowStartIdx = Math.max(0,rowStartIdxAndCount[0]);
							if (rowStartIdx > 0) {
								query.setFirstResult(rowStartIdx);
							}
			
							if (rowStartIdxAndCount.length > 1) {
						    	int rowCount = Math.max(0,rowStartIdxAndCount[1]);
						    	if (rowCount > 0) {
						    		query.setMaxResults(rowCount);    
						    	}
							}
						}										
						return query.getResultList();
			} catch (RuntimeException re) {
							EntityManagerHelper.log("find by property name failed", Level.SEVERE, re);
					throw re;
			}
		}	
	
	
	public List<UserLang> findByPropertyIn(String propertyName, final List value
	        , final int...rowStartIdxAndCount
	        ) {
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("ids", ids);
		String jpql = "select o from UserLang o where o.langId in(:ids)";
		List<UserLang> modules = getEntityManager().createQuery(jpql).setParameter("ids", value).getResultList();
	    		return modules;
		}			
	
	
	public static void main(String[] args) {
		/* in 查询
		UserLangDao dao = new UserLangDao();
		
		List<Integer> ids = new ArrayList<Integer>();
		ids.add(1);
		ids.add(2);
		List<UserLang> list = dao.findByPropertyIn("langId", ids);
		System.out.println(list.size());*/

		getEntityManager().getTransaction().begin();

		Query query = getEntityManager().createNativeQuery("delete from user_lang where userId='aaabb'");
//		List list= query.getResultList();
//		System.out.println("list: " + list);

		int res = query.executeUpdate();
		System.out.println( "delete:" + res);
		getEntityManager().getTransaction().commit();
	}
}
