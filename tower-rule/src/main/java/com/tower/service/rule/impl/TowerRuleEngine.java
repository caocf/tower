/**
 * 
 */
package com.tower.service.rule.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.codehaus.plexus.util.FileUtils;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.io.KieResources;
import org.kie.api.runtime.KieContainer;

import com.tower.service.log.Logger;
import com.tower.service.log.LoggerFactory;
import com.tower.service.rule.IEngine;

/**
 * @author alexzhu
 * 
 */
public abstract class TowerRuleEngine<T extends TowerSession> implements IEngine<T> {

	private String kieBaseName = "FileSystemBase";
	private String packages = "rules";
	private String sessionName = "FileSystemKSession";
	private String path;
	private static KieServices kieService = KieServices.Factory.get();
	private KieContainer kContainer = kieService.getKieClasspathContainer();

	private KieResources resources;
	private KieFileSystem fileSystem;
	protected Logger logger = LoggerFactory.getLogger(getClass());
	public TowerRuleEngine() {
		this(null);
		System.setProperty("drools.dateformat", "yyyy-MM-dd HH:mm:ss");
	}

	public TowerRuleEngine(String sessionName) {
		kContainer = kieService.getKieClasspathContainer();
		
	}

	public KieContainer getContainer() {
		return kContainer;
	}

	public KieContainer getkContainer() {
		return kContainer;
	}

	public void setkContainer(KieContainer kContainer) {
		this.kContainer = kContainer;
	}

	public static KieServices getKieService() {
		return kieService;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public KieFileSystem getFileSystem() {
		return fileSystem;
	}

	public void setFileSystem(KieFileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	public void setKieBaseName(String kieBaseName) {
		this.kieBaseName = kieBaseName;
	}

	public String getKieBaseName(){
		return "FileSystemBase";
	}
	
	public void setPackages(String packages) {
		this.packages = packages;
	}
	
	public String getPackages(){
		return packages;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	public String getSessionName(){
		return sessionName;
	}
	
	public void refresh() {
		
		KieResources resources = getKieService().getResources();
		
		KieModuleModel kieModuleModel = getKieService().newKieModuleModel();// 1

		KieBaseModel baseModel = kieModuleModel.newKieBaseModel(
				this.getKieBaseName()).addPackage(getPackages());// 2
		
		baseModel.newKieSessionModel(getSessionName());// 3

		fileSystem = getKieService().newKieFileSystem();

		String xml = kieModuleModel.toXML();
		
		logger.info("KieModuleXml: "+xml);
		
		fileSystem.writeKModuleXML(xml);// 5
		
		String fileBasePath = Thread.currentThread().getContextClassLoader()
				.getResource("").getPath();
		logger.info("FileBathPath: "+fileBasePath);
		fileBasePath = fileBasePath.substring(0, fileBasePath.length());

		List<String> fileList=null;
		try {
			fileList = FileUtils.getDirectoryNames(new File(fileBasePath), ".drl", null, false);
			for (String sfile : fileList) {
				fileSystem.write("/config/rules/Rule.drl",
						resources.newFileSystemResource(new File(sfile)));// 6
			}
			KieBuilder kb = getKieService().newKieBuilder(fileSystem);
			kb.buildAll();// 7
			if (kb.getResults().hasMessages(Level.ERROR)) {
				logger.error("Build Errors:\n"+kb.getResults().toString());
			}
			kContainer = getKieService().newKieContainer(
					getKieService().getRepository().getDefaultReleaseId());
		} catch (IOException e) {
			logger.error("Build Errors",e);
		}
	}

	public void refreshRule(String ruleFile){
		fileSystem.write(ruleFile,
				resources.newFileSystemResource(path));// 6
		KieBuilder kb = getKieService().newKieBuilder(fileSystem);
		kb.buildAll();// 7
		if (kb.getResults().hasMessages(Level.ERROR)) {
			logger.error("Build Errors:\n"+kb.getResults().toString());
		}
		kContainer = getKieService().newKieContainer(
				getKieService().getRepository().getDefaultReleaseId());
	}
	
	public void execute(){
		T session = this.build();
		session.fireAllRules();
		session.dispose();
	}
	
	public void execute(T ksession){
		if(ksession!=null){
			ksession.fireAllRules();
			ksession.dispose();
		}
	}
}
