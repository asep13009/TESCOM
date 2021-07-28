package com.asep.test.testno2.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestControllerCRUD {
	@Autowired
	Table1Repo repo;
	
	@RequestMapping(method = RequestMethod.POST, path = "insert")
	public Map<String , Object>  create( @RequestBody Table1 table1) { 
		Map<String , Object> map = new HashMap<String, Object>();
		if(repo.existsById(table1.getId())) {
			map.put("rd", "data present with id "+ table1.getId());
		}else {
			repo.save(table1);
			map.put("rd", "saved");
			map.put("data", table1);
		} 
		return map;
	}
	
	
	
	@RequestMapping(method = RequestMethod.GET, path = "read/{id}")
	public Map<String , Object>  read(@PathVariable Integer id) {
		Map<String , Object> result = new HashMap<String, Object>();
		try { 
			result.put("data", repo.findById(id).get());
		} catch (Exception e) {
			// TODO: handle exception
			result.put("rd", "id notfound");
		}
		
		return result;
	}
	
	@RequestMapping(method = RequestMethod.POST, path = "update/{id}")
	public Map<String , Object> update(@PathVariable Integer id, @RequestBody Table1 table1) {
		Map<String , Object> restult= new HashMap<String, Object>();
		if(repo.findById(id).isPresent()) { 
			table1.setId(id);
			repo.save(table1);
			restult.put("rd", "success");
			restult.put("data", table1);
			
		}else {
			restult.put("rd", "failed");
		}
		return restult;
	}
	

	
	
	@RequestMapping(method = RequestMethod.POST, path = "delete/{id}")
	public Map<String , Object> delete(@PathVariable Integer id) {
		Map<String , Object> restult= new HashMap<String, Object>();
		if(repo.findById(id).isPresent()) {	
			Table1 table1 = repo.findById(id).get();
			repo.delete(table1);
			restult.put("rd", "deleted");
			restult.put("data", table1); 
		}else {
			restult.put("rd", "failed");
		} 
		return restult;
	}
	
	
	
	
	
	
}
