package com.ikaz.demo.tftp.ws.domain;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * This POJO represents a file
 * @author icastillejos
 * @version 0.0.1
 */
@XmlRootElement(name="tftpfile")
public class TFTPFile {
	private String id;
	private String filePath;
	private String stringContent;
	private byte[] byteContent;
	private int blockNumber;
	private List<Link> links;
	
	public TFTPFile(){
		super();
	}
	
	public TFTPFile(String id, String filePath){
		super();
		this.id = id;
		this.filePath = filePath;
	}
	
	@XmlAttribute
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	@XmlElement
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	@XmlElement
	public int getBlockNumber() {
		return blockNumber;
	}

	public void setBlockNumber(int blockNumber) {
		this.blockNumber = blockNumber;
	}

	@XmlElement
	public String getStringContent() {
		return stringContent;
	}

	public void setStringContent(String stringContent) {
		this.stringContent = stringContent;
	}

	@XmlElement(name="link")
	@XmlJavaTypeAdapter(Link.JaxbAdapter.class)
	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}
	
	@XmlTransient
	public URI getNext()
	{
		if (links == null) return null;
		for (Link link : links){
			if ("next".equals(link.getRel())){
				return link.getUri();
			}
		}
		return null;
	}
	
	@XmlTransient
	public URI getPrevious()
	{
		if (links == null) return null;
		for (Link link : links){
			if ("previous".equals(link.getRel())){
				return link.getUri();
			}
		}
		return null;
	}
}
