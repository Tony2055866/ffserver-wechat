package com.wc.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="user_lang"
    ,catalog="openfire"
)
public class UserLang  implements java.io.Serializable{
	@Id 
	@Column(name="userId")
	 private String userId;
	
	@Id 
	 @Column(name="langId")
     private Integer langId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Integer getLangId() {
		return langId;
	}

	public void setLangId(Integer langId) {
		this.langId = langId;
	}

	@Override
	public String toString() {
		return "UserLang [langId=" + langId + ", userId=" + userId + "]";
	}
	 
	 
}
