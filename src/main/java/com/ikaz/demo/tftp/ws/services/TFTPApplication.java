package com.ikaz.demo.tftp.ws.services;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * This 
 * @author icastillejos
 * @version 1.0.0
 */
@ApplicationPath("/services")
public class TFTPApplication extends Application{
	private Set<Object> singletons  = new HashSet<Object>();
	
	public TFTPApplication(){
	singletons.add(new TFTPResource());
	}
	
	@Override
	public Set<Object> getSingletons(){
		return singletons;
	}

}
