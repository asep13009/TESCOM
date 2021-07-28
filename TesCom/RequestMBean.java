package bankmega.project.controller;

import bankmega.app.model.User;
import bankmega.app.repo.UserRepo;
import bankmega.app.web.model.SecureItem;
import bankmega.app.web.util.AbstractManagedBean;
import bankmega.app.web.util.LazyDataModelJPA;
import bankmega.project.model.*;
import bankmega.project.model.request.MsKodePos;
import bankmega.project.model.slik.Credit;
import bankmega.project.model.slik.ResultIdeb;
import bankmega.project.repo.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.json.JSONObject;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA. User : taufik.budiyanto Date & Time : 28/08/2019 &
 * 09:42
 */
@Controller
@Data
@Scope("view")
public class RequestMBean extends AbstractManagedBean implements InitializingBean {
	private String urlPdfSlik;
	private String typeIncome;
	private String pdfSlik;
	private TreeNode root;
	private LazyDataModelJPA<Request> listRequest;
	private String out;
	private LazyDataModelJPA<Request> listRequestSupple;
	private String gajibulan;
	private Double salary;
	private String gajiTahun;
	private String urlKtp;
	private String id;
	private Request reqSelected;
	private Double periodeSpt;
	private List<String> imagesLain;
	private String pathNpwp = "";
	@Autowired
	private FileLocationRepo fileLocationRepo;

	@Autowired
	private RejectReasonRepo rejectReasonRepo;
	@Autowired
	private CommentRepo commentRepo;

	@Autowired
	private UserRepo userRepo;
	@Autowired
	private TmMsmileApiRepo tmMsmileApiRepo;

	@Value("${rek.db.url}")
	private String dbUrlRek;
	@Value("${rek.db.user}")
	private String dbUserRek;
	@Value("${rek.db.pass}")
	private String dbPassRek;

	private List<Request> losList;
	private List<RequestNTB> losListNTB;
	private List<User> listUser;
	private String ketIncome;
	private Boolean showDetail;
	private Boolean saveRecalculate;
	private Double limit;
	private String type;
	private String nads;
	private List<DataSupplement> listDataSupplement;
	private List<String> images;
	private String nama;
	private RejectReason rejectReason;
	private DecissionAnalyst decision;
	private String field;
	private String value;
	private String dokumenSalary;
	@Autowired
	private RequestRepo reqRepo;
	 @Autowired
	private RequestNtbRepo reqRepoNTB;
	private Double hasilDbr;
	private String userlogin;
	private String userApproval;
	private Boolean submitApproval;
	private Boolean reject;
	private Boolean save;
	private Double bwmk;
	private Double angkaD;
	private String typePekerjaan;
	private Double bwmk2;
	private List<RejectReason> listReject;
	private String comment;
	private Boolean approve;
	private String txt2;
	private Boolean spt;
	private String kodePos;
	private List<Object> listIdeb;
	private List<Object> listSelectIdebPefindo;
	private List<Object> listSelectIdeb;
	private List<Credit> listCredit = new ArrayList<Credit>();
	private List<FileLocation> fileLocations = new ArrayList<>();
	private List<RejectReason> rejectReasons = new ArrayList<>();
	private Boolean showElement = true;
	private Long totalOSCC;
	private Long totalOSLoan;
	private Long totalLimitCC, totalLimitLoan;
	
	String pathImage;
	String pathSelfie;
	
	
	private String typelogin;

	@Override
	protected List<SecureItem> getSecureItems() {
		return null;
	}

	private String toDataSalary(Double sal) {
		try {
			return String.format("%.0f", sal);
		} catch (Exception e) {
			return "0";
		}

	}

	public List<MsKodePos> completeText(String query) {
		List<MsKodePos> allThemes = new ArrayList<>();
		try {
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<MsKodePos[]> response2 = restTemplate
					.getForEntity("http://10.14.19.57:8500/api/kodepos/" + query, MsKodePos[].class);
			allThemes = Arrays.asList(response2.getBody());
		} catch (Exception e) {
		}
		return allThemes;

	}

	public void ajaxdata2() {
		try {
			System.out.println(angkaD + "============");
			if (angkaD == 0) {
				RequestContext.getCurrentInstance().execute("alert('Harap Masukkan Salary');");
				return;
			}
			if (angkaD == null) {
				RequestContext.getCurrentInstance().execute("alert('Harap Masukkan Salary');");
				return;
			}
			if (reqSelected.getTypeIncome() == null || reqSelected.getTypeIncome().equals("-")) {
				RequestContext.getCurrentInstance().execute("alert('Harap Masukkan Type Type Income');");
				return;
			}
			try {
//                calculateDbr();
				if (reqSelected.getSalaryMonth() == 0) {
					RequestContext.getCurrentInstance().execute("alert('Income Document Masih kosong...!');");
					return;
				}
			} catch (Exception e) {

			}
			reqSelected.setSalary(String.format("%.0f", reqSelected.getSalaryMonth() * 12));
			save = true;
			reqRepo.save(reqSelected);
			RequestContext.getCurrentInstance().update("req-content");
		} catch (Exception e) {
			RequestContext.getCurrentInstance().execute("alert('----------Please Complete all------------');");
		}
	}

	public void ajaxdata() {
		try {
//            reqSelected=calculateDbrLib(reqSelected,salary,periodeSpt);

		} catch (Exception e) {
		}
		reqRepo.save(reqSelected);
		try {
			if (reqSelected.getDbr().intValue() > 35) {
				reject = true;
				approve = false;
			} else {
				reject = false;
				approve = true;
			}

		} catch (Exception e) {
		}
	}

	public void editAssign(SelectEvent event) {
		String itemSelect = (String) event.getObject();
		System.out.println("SELECTED ITEM : " + itemSelect);
		if (itemSelect.equals("2")) {
			listUser = userRepo.findAllByType("prescreen");
			reqSelected.setStage(5);
//            reqSelected.setUserPrescreen(nipAnalyst);
		} else {
			List<User> users = new ArrayList<User>();
			System.out.println(reqSelected.getJenisLaporan());
			if (reqSelected.getJenisLaporan().equals("Naik Limit")) {
				
				users = userRepo.findAllByType("analyst");
				System.out.println("LIST USER : " + users.size());
				System.out.println(users);
				listUser = users.stream().filter(a -> a.getUsername() != userlogin).collect(Collectors.toList());
//                listUser = users;
			} else if (reqSelected.getJenisLaporan().equals("Perubahan Data CCBM")) {
				users = userRepo.findAllByType("analyst_tma");
				listUser = users.stream().filter(a -> a.getUsername() != userlogin).collect(Collectors.toList());
			}else {
//				listUser = users.stream().filter(a -> a.getBwmk() > bwmk).collect(Collectors.toList());
				users = userRepo.findAllByType("analyst");
				System.out.println("LIST USER : " + users.size());
				System.out.println(users);
				listUser = users.stream().filter(a -> a.getUsername() != userlogin).collect(Collectors.toList());
				System.out.println("SOURCE CODE :" + reqSelected.getSourceCode());
				try {
					if (reqSelected.getSourceCode().substring(2).equals("PA")) {
						listUser = users.stream().filter(a -> a.getBwmkInvinite() > bwmk).collect(Collectors.toList());
					}
				} catch (Exception e) {
				}

			}
			reqSelected.setStage(2);
			reqSelected.setUserApproval(nipAnalyst);
		}
		if (reqSelected.getDecissionAnalyst().getId() == null) {
			DecissionAnalyst decissionAnalyst = new DecissionAnalyst();
			decissionAnalyst.setId(new Date().getTime() + "");
			reqSelected.setDecissionAnalyst(decissionAnalyst);
		}
//		reqRepo.save(reqSelected);

	}

	public void editIncome() {
		try {
			if (reqSelected.getTypeIncome().equals("No Income Document")) {
				approve = false;
				reject = true;
			} else {
				approve = true;
				reject = false;
			}
			if (reqSelected.getTypeIncome().equals("Slip Gaji")) {
				ketIncome = "Salary / Bulan";
			} else if (reqSelected.getTypeIncome().equals("SPT")) {
				ketIncome = "Salary Periode";
			} else if (reqSelected.getTypeIncome().equals("SKP")) {
				ketIncome = "Salary / Bulan";
			} else if (reqSelected.getTypeIncome().equals("Mutasi Rekening")) {
				ketIncome = "Avg Amount/Bulan";
			} else if (reqSelected.getTypeIncome().equals("SALDO")) {
				ketIncome = "Avg Amount/Bulan";
			} else if (reqSelected.getTypeIncome().equals("No Income Document")) {
				ketIncome = "Salary / Bulan";
			} else if (typeIncome.equals("Surrogate Income")) {
				ketIncome = "Salary / Bulan";
			}
			angkaD = null;
		} catch (Exception e) {

		}

	}

	public List<MsKodePos> completeTextKota(String query) {
		List<MsKodePos> allThemes = new ArrayList<>();
		try {
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<MsKodePos[]> response2 = restTemplate
					.getForEntity("http://10.14.19.57:8500/api/kota/" + query, MsKodePos[].class);
			allThemes = Arrays.asList(response2.getBody());
		} catch (Exception e) {
			return new ArrayList<>();
		}
		return allThemes;

	}

	public List<MsKodePos> completeTextKec(String query) {
		List<MsKodePos> allThemes = new ArrayList<>();
		try {
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<MsKodePos[]> response2 = restTemplate
					.getForEntity("http://10.14.19.57:8500/api/kecamatan/" + query, MsKodePos[].class);
			allThemes = Arrays.asList(response2.getBody());
		} catch (Exception e) {
		}
		return allThemes;

	}

	public List<MsKodePos> completeTextKel(String query) {
		List<MsKodePos> allThemes = new ArrayList<>();
		try {
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<MsKodePos[]> response2 = restTemplate
					.getForEntity("http://10.14.19.57:8500/api/kelurahan/" + query, MsKodePos[].class);
			allThemes = Arrays.asList(response2.getBody());
		} catch (Exception e) {
		}
		return allThemes;

	}

	public void onItemSelect(SelectEvent event) {
		MsKodePos kodePosEvent = (MsKodePos) event.getObject();
		reqSelected.getDecissionAnalyst().setKodepos(kodePosEvent.getZipCode());
//        reqSelected.getDecissionAnalyst().setPropinsi(kodePosEvent.getPropinsi());
		reqSelected.getDecissionAnalyst().setKota(kodePosEvent.getKota());
		reqSelected.getDecissionAnalyst().setKecamtan(kodePosEvent.getKecamatan());
		reqSelected.getDecissionAnalyst().setKelurahan(kodePosEvent.getKelurahan());
		RequestContext.getCurrentInstance().update("req-content");
	}

	public void changeReject() {
		if (rejectReason == null) {
			reject = false;
			approve = true;
			if (reqSelected.getTypeIncome().equals("No Income Document")) {
				reject = true;
				approve = false;
			}

		} else {
			reject = true;
			approve = false;
		}
		RequestContext.getCurrentInstance().update("req-content");
	}

	private DataSupplement dataSupplement;

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("TES MASUK");
		listDataSupplement = new ArrayList<>();
		losList = new ArrayList<>();
		supplement = false;
		approve = true;
		assign = false;

		reject = false;
		listReject = new ArrayList<>();
		listUser = new ArrayList<>();

		submitApproval = false;
		hasilDbr = 0.0;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) authentication.getPrincipal();
		typelogin = user.getParty().getFirstName();
		try {
			bwmk = user.getBwmk();

		} catch (Exception e) {
			bwmk = 0.0;
		}
		try {
			bwmk2 = user.getBwmkInvinite();
		} catch (Exception e) {
			bwmk2 = 0.0;
		}
		userlogin = authentication.getName();
		showDetail = false;
		saveRecalculate = false;
		listRequest = new LazyDataModelJPA(reqRepo) {
			@Override
			protected long getDataSize() {
				return reqRepo.count(whereQuery());
			}

			@Override
			protected Page getDatas(PageRequest request) {
				return reqRepo.findAll(whereQuery(), request);
			}
		};
		images = new ArrayList<>();
		listReject = rejectReasonRepo.findAll();
	}

	public void onToggle(ToggleEvent event) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, event.getComponent().getId() + " toggled",
				"Status:" + event.getVisibility().name());
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void findData() {
		listRequest = new LazyDataModelJPA(reqRepo) {
			@Override
			protected long getDataSize() {
				return reqRepo.count(whereQuery());
			}

			@Override
			protected Page getDatas(PageRequest request) {
				return reqRepo.findAll(whereQuery(), request);
			}
		};
	}

	private Boolean supplement;

	public void editSuple(DataSupplement supple) {
		reqRepo.save(reqSelected); 
	}

	List<DedupDwh> listDwh;

	public void loadDetail() {
		  if(reqSelected.getJenisLaporan().equals("Naik Limit")){ 
	        	loadDetailNaikLimit(); 
	        }else if(reqSelected.getJenisLaporan().equals("Reinstate")){
	        	loadDetailReinstate();
	        }else if(reqSelected.getJenisLaporan().equals("Perubahan Data CCBM")) {
	        	loadDetailPrrdatCCBM(); 
	        }
	}

	private void loadDetailPrrdatCCBM() {
		listDwh = new ArrayList<DedupDwh>();
		decision = new DecissionAnalyst();
		reqSelected.setDecissionAnalyst(decision);

		 
		RequestContext.getCurrentInstance().execute("hideComponentForChange();");
		
		losList = getDataLOS(reqSelected.getJenisLaporan());
		
		
		try {
			if (reqSelected.getDedupDwhs().size() > 0) {
				for (DedupDwh ddwh : reqSelected.getDedupDwhs()) {
					if (ddwh.getTypeCard().contains("P")) {
						listDwh.add(ddwh);
					}

				}

			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (reqSelected.getListResultIdeb().size() > 0) {
			for (ResultIdeb ideb : reqSelected.getListResultIdeb()) {
				listCredit = ideb.getResultIdeb().getIndividual().getFasilitas().getKreditPembiayan();
			}
			totalOSCC = listCredit.stream()
					.filter(f -> f.getJenisKreditPembiayaan().equals("30") && !f.getKondisiKet().equals("Lunas"))
					.mapToLong(x -> Long.valueOf(x.getBakiDebet())).sum();
			totalOSLoan = listCredit.stream()
					.filter(f -> !f.getJenisKreditPembiayaan().equals("30") && !f.getKondisiKet().equals("Lunas"))
					.mapToLong(x -> Long.valueOf(x.getBakiDebet())).sum();

			totalLimitCC = listCredit.stream().filter(
					f -> f.getJenisKreditPembiayaan().equals("30") && f.getKondisiKet().equals("Fasilitas Aktif"))
					.mapToLong(x -> (long) Double.parseDouble(x.getPlafon())).sum();
			totalLimitLoan = listCredit.stream().filter(
					f -> !f.getJenisKreditPembiayaan().equals("30") && f.getKondisiKet().equals("Fasilitas Aktif"))
					.mapToLong(x -> {
						System.out.println(x.getPlafon() + " | " + (long) Double.parseDouble(x.getPlafonAwal()));
						return (long) Double.parseDouble(x.getPlafonAwal());

					}).sum();
		}

		if (reqSelected.getIncomeAkki() != null) {
			Double incomeAkki = Double.valueOf(reqSelected.getIncomeAkki()) * 12;
			Double incomeAscend = Double.valueOf(reqSelected.getSalary());
			reqSelected.setAkki(String.valueOf(incomeAkki));
			if (incomeAkki > incomeAscend) {
				BigInteger incomeAnnual = BigDecimal.valueOf(incomeAkki).toBigInteger();
				Double incomeMonth = incomeAkki / 12;
				reqSelected.setSalaryAnnual(String.valueOf(incomeAnnual));
				reqSelected.setSalaryMonth(incomeMonth);
			} else {
				BigInteger incomeAnnual = BigDecimal.valueOf(incomeAscend).toBigInteger();
				Double incomeMonth = incomeAscend / 12;
				reqSelected.setSalaryAnnual(String.valueOf(incomeAnnual));
				reqSelected.setSalaryMonth(incomeMonth);
			}
		} else {
			reqSelected.setSalaryAnnual(reqSelected.getSalaryAnnual());
			if (reqSelected.getSalaryAnnual() != null && !reqSelected.getSalaryAnnual().isEmpty()
					&& !reqSelected.getSalaryAnnual().equals("")) {
				reqSelected.setSalaryMonth(Double.parseDouble(reqSelected.getSalaryAnnual()) / 12);
			}
		}
		  
		refreshComment();
 
		  

		if (reqSelected.getListFile().size() > 0) {
			fileLocations = reqSelected.getListFile();
		}

 
		try {
			if (reqSelected.getTypeIncome().equals("No Income Document")) {
				approve = false;
				reject = true;
			}
		} catch (Exception e) {

		}
		if (reqSelected.getListDataSuplement().size() > 0) {
			supplement = true;
			listDataSupplement = reqSelected.getListDataSuplement();
		}
		try {
//            kodePos=kodePosRepo.findAllByZipCode(reqSelected.getAddress().getKodePos()).get(0);
		} catch (Exception e) {

		}
		try {
			if (reqSelected.getSourceCode().substring(0, 2).equals("PA")) {
				bwmk = bwmk2;
			}
		} catch (Exception e) {

		}
		try {
			salary = reqSelected.getSalaryMonth();
		} catch (Exception e) {
			salary = 0.0;
		}
		try {
			imagesLain = new ArrayList<>();
//            String [] arrTemp = new String[] {"selfie.jpeg","ktp.jpeg"};
			String[] arrTemp = new String[] { "0.png", "1.png" };
			List<String> notIn = Arrays.asList(arrTemp);
			int a = 1;
			
			

			 
	        System.out.println(""+ reqSelected.getJenisLaporan());
	        pathImage="bankmega/PERUBAHAN_DATA/IMG/"; 
	    	pathNpwp = pathImage+reqSelected.getReffId()+"/"+reqSelected.getReffId()+"-KTP.png";  
	    	pathSelfie = pathImage+reqSelected.getReffId()+"/"+reqSelected.getReffId()+"Selfie.png"; 
	        	
	      
	            imagesLain = new ArrayList<>(); 
	          
//	            for(int u= 0; u<=13; u++) {
	                imagesLain.add(pathImage+reqSelected.getReffId()+"/"+reqSelected.getReffId()+"-Selfie.png");
	                imagesLain.add(pathImage+reqSelected.getReffId()+"/"+reqSelected.getReffId()+"-CC.png");
	                imagesLain.add(pathImage+reqSelected.getReffId()+"/"+reqSelected.getReffId()+"-KTP.png");
	                
//	              }
			
			

//			for (int i = 1; i <= 13; i++) {
//				imagesLain.add("-" + a++ + ".png");
//			}
//			imagesLain.add(".png");
//			System.out.println(imagesLain);
//            for(FileLocation fileLocation :reqSelected.getListFile()){
//            	System.out.println(fileLocation);
//                if(!notIn.contains(fileLocation.getFilename())) {
//                    imagesLain.add(fileLocation.getLocation());
//                }
//            }

		} catch (Exception e) {

		}
//        urlKtp="../../resources/bankmega/"+reqSelected.getReffId()+"/ktp.jpeg";
//        dokumenSalary="../../resources/bankmega/"+reqSelected.getReffId()+"/slip.jpeg";

		comment = "";
		rejectReason = null;
		showDetail = true;
		try {
			reqSelected.getDecissionAnalyst().setSalaryTemp(toDataSalary(reqSelected.getSalaryMonth() * 12));
			reqSelected.getDecissionAnalyst().setSalaryMonthTemp(toDataSalary(reqSelected.getSalaryMonth()));
			reqSelected.getDecissionAnalyst().setCatatan("");
		} catch (Exception e) {
		}

		try {
			List<User> users = userRepo.findAllByType("analyst_tma");
//			listUser = users.stream().filter(a -> a.getBwmk() > bwmk).collect(Collectors.toList());
			listUser = users;
			if (reqSelected.getSourceCode().substring(0, 2).equals("PA")) {
				listUser = users.stream().filter(a -> a.getBwmkInvinite() > bwmk).collect(Collectors.toList());
			}
		} catch (Exception e) {

		}
		
	}

	private void loadDetailReinstate() {
		listDwh = new ArrayList<DedupDwh>();
		decision = new DecissionAnalyst();
		reqSelected.setDecissionAnalyst(decision);

		 
		losListNTB = getDataLOSREINS();
		
		
		try {
			if (reqSelected.getDedupDwhs().size() > 0) {
				for (DedupDwh ddwh : reqSelected.getDedupDwhs()) {
					if (ddwh.getTypeCard().contains("P")) {
						listDwh.add(ddwh);
					}

				}

			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (reqSelected.getListResultIdeb().size() > 0) {
			for (ResultIdeb ideb : reqSelected.getListResultIdeb()) {
				listCredit = ideb.getResultIdeb().getIndividual().getFasilitas().getKreditPembiayan();
			}
			totalOSCC = listCredit.stream()
					.filter(f -> f.getJenisKreditPembiayaan().equals("30") && !f.getKondisiKet().equals("Lunas"))
					.mapToLong(x -> Long.valueOf(x.getBakiDebet())).sum();
			totalOSLoan = listCredit.stream()
					.filter(f -> !f.getJenisKreditPembiayaan().equals("30") && !f.getKondisiKet().equals("Lunas"))
					.mapToLong(x -> Long.valueOf(x.getBakiDebet())).sum();

			totalLimitCC = listCredit.stream().filter(
					f -> f.getJenisKreditPembiayaan().equals("30") && f.getKondisiKet().equals("Fasilitas Aktif"))
					.mapToLong(x -> (long) Double.parseDouble(x.getPlafon())).sum();
			totalLimitLoan = listCredit.stream().filter(
					f -> !f.getJenisKreditPembiayaan().equals("30") && f.getKondisiKet().equals("Fasilitas Aktif"))
					.mapToLong(x -> {
						System.out.println(x.getPlafon() + " | " + (long) Double.parseDouble(x.getPlafonAwal()));
						return (long) Double.parseDouble(x.getPlafonAwal());

					}).sum();
		}

		 
		  
		refreshComment();
 
	 
	 

		if (reqSelected.getListFile().size() > 0) {
			fileLocations = reqSelected.getListFile();
		}

		try {
			List<User> users = userRepo.findAllByType("analyst");
//            System.out.println(reqSelected.getSourceCode().substring(0,2));
//            listUser = users.stream().filter(a-> a.getBwmk() > bwmk).collect(Collectors.toList());
			listUser = users;
			 
				if (reqSelected.getSourceCode().substring(0, 2).equals("PA")) {
					listUser = users.stream().filter(a -> a.getBwmkInvinite() > bwmk).collect(Collectors.toList());
			 
				}

		} catch (Exception e) {
//            e.printStackTrace();
		}
		try {
			if (reqSelected.getTypeIncome().equals("No Income Document")) {
				approve = false;
				reject = true;
			}
		} catch (Exception e) {

		}
		if (reqSelected.getListDataSuplement().size() > 0) {
			supplement = true;
			listDataSupplement = reqSelected.getListDataSuplement();
		}
		try {
//            kodePos=kodePosRepo.findAllByZipCode(reqSelected.getAddress().getKodePos()).get(0);
		} catch (Exception e) {

		}
		try {
			if (reqSelected.getSourceCode().substring(0, 2).equals("PA")) {
				bwmk = bwmk2;
			}
		} catch (Exception e) {

		}
		try {
			salary = reqSelected.getSalaryMonth();
		} catch (Exception e) {
			salary = 0.0;
		}
		try {
			imagesLain = new ArrayList<>();
//            String [] arrTemp = new String[] {"selfie.jpeg","ktp.jpeg"};
			String[] arrTemp = new String[] { "0.png", "1.png" };
			List<String> notIn = Arrays.asList(arrTemp);
			int a = 1;


			pathImage="bankmega/REINSTATE/IMG/";
	         
        	pathNpwp = pathImage+reqSelected.getReffId()+"/"+reqSelected.getReffId()+"-1.png"; 
        	pathSelfie = pathImage+reqSelected.getReffId()+"/"+reqSelected.getReffId()+"-0.png"; 
        	
        	imagesLain=new ArrayList<String>();
        	  
        	 
             
            for(int u= 0; u<=13; u++) {
              imagesLain.add(pathImage+reqSelected.getReffId()+"/"+reqSelected.getReffId()+"-"+ u +".png");
            }
             
			
			
			
//			for (int i = 1; i <= 13; i++) {
//				imagesLain.add("-" + a++ + ".png");
//			}
//			imagesLain.add(".png");
//			System.out.println(imagesLain);
//            for(FileLocation fileLocation :reqSelected.getListFile()){
//            	System.out.println(fileLocation);
//                if(!notIn.contains(fileLocation.getFilename())) {
//                    imagesLain.add(fileLocation.getLocation());
//                }
//            }

		} catch (Exception e) {

		}
//        urlKtp="../../resources/bankmega/"+reqSelected.getReffId()+"/ktp.jpeg";
//        dokumenSalary="../../resources/bankmega/"+reqSelected.getReffId()+"/slip.jpeg";

		comment = "";
		rejectReason = null;
		showDetail = true;
		try {
			reqSelected.getDecissionAnalyst().setSalaryTemp(toDataSalary(reqSelected.getSalaryMonth() * 12));
			reqSelected.getDecissionAnalyst().setSalaryMonthTemp(toDataSalary(reqSelected.getSalaryMonth()));
			reqSelected.getDecissionAnalyst().setCatatan("");
		} catch (Exception e) {
		}
		//penyesuaian yola
		reqSelected.setSalaryAnnual(reqSelected.getSalary());
		

		try {
			List<User> users = userRepo.findAllByType("analyst");
			listUser = users.stream().filter(a -> a.getBwmk() > bwmk).collect(Collectors.toList());
			if (reqSelected.getSourceCode().substring(0, 2).equals("PA")) {
				listUser = users.stream().filter(a -> a.getBwmkInvinite() > bwmk).collect(Collectors.toList());
			}
		} catch (Exception e) {

		}
		
	}

	private void loadDetailNaikLimit() {
		pathImage="bankmega/NAIK_LIMIT/";
		listDwh = new ArrayList<DedupDwh>();
		decision = new DecissionAnalyst();
		reqSelected.setDecissionAnalyst(decision);

		if (reqSelected.getJenisLaporan().equals("Naik Limit")) {
			String dateCreated = new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(new Date());
			RequestContext.getCurrentInstance().execute("hideComponentForIncreaseLimit();");
		}
		
		losList = getDataLOS(reqSelected.getJenisLaporan());
		
		try {
			if (reqSelected.getDedupDwhs().size() > 0) {
				for (DedupDwh ddwh : reqSelected.getDedupDwhs()) {
					if (ddwh.getTypeCard().contains("P")) {
						listDwh.add(ddwh);
					}
				}
			}
		} catch (Exception e) {}

		if (reqSelected.getListResultIdeb().size() > 0) {
			for (ResultIdeb ideb : reqSelected.getListResultIdeb()) {
				listCredit = ideb.getResultIdeb().getIndividual().getFasilitas().getKreditPembiayan();
			}
			totalOSCC = listCredit.stream()
					.filter(f -> f.getJenisKreditPembiayaan().equals("30") && !f.getKondisiKet().equals("Lunas"))
					.mapToLong(x -> Long.valueOf(x.getBakiDebet())).sum();
			totalOSLoan = listCredit.stream()
					.filter(f -> !f.getJenisKreditPembiayaan().equals("30") && !f.getKondisiKet().equals("Lunas"))
					.mapToLong(x -> Long.valueOf(x.getBakiDebet())).sum();

			totalLimitCC = listCredit.stream().filter(
					f -> f.getJenisKreditPembiayaan().equals("30") && f.getKondisiKet().equals("Fasilitas Aktif"))
					.mapToLong(x -> (long) Double.parseDouble(x.getPlafon())).sum();
			totalLimitLoan = listCredit.stream().filter(
					f -> !f.getJenisKreditPembiayaan().equals("30") && f.getKondisiKet().equals("Fasilitas Aktif"))
					.mapToLong(x -> {
						System.out.println(x.getPlafon() + " | " + (long) Double.parseDouble(x.getPlafonAwal()));
						return (long) Double.parseDouble(x.getPlafonAwal());

					}).sum();
		}

		if (reqSelected.getIncomeAkki() != null) {
			Double incomeAkki = Double.valueOf(reqSelected.getIncomeAkki()) * 12;
			Double incomeAscend = Double.valueOf(reqSelected.getSalary());
			reqSelected.setAkki(String.valueOf(incomeAkki));
			if (incomeAkki > incomeAscend) {
				BigInteger incomeAnnual = BigDecimal.valueOf(incomeAkki).toBigInteger();
				Double incomeMonth = incomeAkki / 12;
				reqSelected.setSalaryAnnual(String.valueOf(incomeAnnual));
				reqSelected.setSalaryMonth(incomeMonth);
			} else {
				BigInteger incomeAnnual = BigDecimal.valueOf(incomeAscend).toBigInteger();
				Double incomeMonth = incomeAscend / 12;
				reqSelected.setSalaryAnnual(String.valueOf(incomeAnnual));
				reqSelected.setSalaryMonth(incomeMonth);
			}
		} else {
			reqSelected.setSalaryAnnual(reqSelected.getSalaryAnnual());
			if (reqSelected.getSalaryAnnual() != null && !reqSelected.getSalaryAnnual().isEmpty()
					&& !reqSelected.getSalaryAnnual().equals("")) {
				reqSelected.setSalaryMonth(Double.parseDouble(reqSelected.getSalaryAnnual()) / 12);
			}
		}

		refreshComment();

		try{
			fileLocations.addAll(reqSelected.getListFile().stream().filter(x->x.getType().equals("pdf")).collect(Collectors.toList()));
		}catch (Exception e){e.printStackTrace();}

		FileLocation fileNpwp = fileLocationRepo.findFirstByFilenameAndRefId("npwp",reqSelected.getReffId());

		if (fileNpwp != null){
			pathNpwp = pathImage+fileNpwp.getLocation()+"/"+fileNpwp.getFilename()+"."+fileNpwp.getType();
			System.out.println(pathNpwp);
		}

		imagesLain = new ArrayList<>();
		System.out.println(reqSelected.getListFile().size());
		for(FileLocation fileLocation :reqSelected.getListFile()){
			System.out.println(fileLocation);
			if (fileLocation.getType().equals("jpg") || fileLocation.getType().equals("png")){
				imagesLain.add(pathImage+fileLocation.getLocation()+"/"+fileLocation.getFilename()+"."+fileLocation.getType());
				System.out.println("FIle Location : "+pathImage+fileLocation.getLocation()+"/"+fileLocation.getFilename()+"."+fileLocation.getType());
			}
		}

		try {
			List<User> users = userRepo.findAllByType("analyst");
			if (reqSelected.getTypeRequest().equals("Naik Limit")) {
				listUser = users;
			} else {
				if (reqSelected.getSourceCode().substring(0, 2).equals("PA")) {
					listUser = users.stream().filter(a -> a.getBwmkInvinite() > bwmk).collect(Collectors.toList());
				}
			}
		} catch (Exception e) {}

		try {
			if (reqSelected.getTypeIncome().equals("No Income Document")) {
				approve = false;
				reject = true;
			}
		} catch (Exception e) {}

		if (reqSelected.getListDataSuplement().size() > 0) {
			supplement = true;
			listDataSupplement = reqSelected.getListDataSuplement();
		}

		try {
			if (reqSelected.getSourceCode().substring(0, 2).equals("PA")) {
				bwmk = bwmk2;
			}
		} catch (Exception e) {}

		try {
			salary = reqSelected.getSalaryMonth();
		} catch (Exception e) {
			salary = 0.0;
		}
		comment = "";
		rejectReason = null;
		showDetail = true;
		try {
			reqSelected.getDecissionAnalyst().setSalaryTemp(toDataSalary(reqSelected.getSalaryMonth() * 12));
			reqSelected.getDecissionAnalyst().setSalaryMonthTemp(toDataSalary(reqSelected.getSalaryMonth()));
			reqSelected.getDecissionAnalyst().setCatatan("");
		} catch (Exception e) {}

		try {
			List<User> users = userRepo.findAllByType("analyst");
			listUser = users.stream().filter(a -> a.getBwmk() > bwmk).collect(Collectors.toList());
			if (reqSelected.getSourceCode().substring(0, 2).equals("PA")) {
				listUser = users.stream().filter(a -> a.getBwmkInvinite() > bwmk).collect(Collectors.toList());
			}
		} catch (Exception e) {}
		
	}

	private List<Request> getDataLOS(String jenisLaporan) {
		Date now = new Date();
		LocalDate nowLocalDate = now.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate oneYearBehind = nowLocalDate.minusYears(1);
		Date oneYearBehindDate = Date.from(oneYearBehind.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		List<Request> request = reqRepo.findAllByDatecreatedBeforeAndDatecreatedAfterAndCustNumberAndJenisLaporan(now,
				oneYearBehindDate, reqSelected.getCustNumber(), jenisLaporan);
//		System.out.println("LIST DATA LOS : " + request);
		return request;
	}
	
	private List<RequestNTB> getDataLOSREINS(){
        Date now = new Date();
        String dateCreated = new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(new Date());
        System.out.println(dateCreated);
        LocalDate nowLocalDate = now.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate oneYearBehind = nowLocalDate.minusYears(1);
        System.out.println(oneYearBehind);
        Date oneYearBehindDate = Date.from(oneYearBehind.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
 
        List<RequestNTB> request = reqRepoNTB.findByCustId(reqSelected.getCustId()); 
        return request;
    }
	

	private void refreshComment() {
		List<Comments> commentsList = commentRepo.findAllByidLike("%" + reqSelected.getReffId() + "%");
		reqSelected.setCommentsList(commentsList);
	}

	public void saveData() {
		decision.setId(reqSelected.getReffId());
//        System.out.println("DESS2 : "+  decision.getFeeCode());
//        System.out.println("DESSS : "+ reqSelected.getDecissionAnalyst());
//        System.out.println("masuk sini ");

		reqSelected.setDecissionAnalyst(decision);
//		System.out.println(reqSelected.getDecissionAnalyst().getCatatan());
//		System.out.println("tes");
		Comments comments = new Comments();
		comments.setUserId(userlogin);
		comments.setComments(reqSelected.getDecissionAnalyst().getCatatan());
		comments.setDateCreated(new Date());
		comments.setId(reqSelected.getReffId() + "-" + getAlphaNumericString(10));
		reqSelected.getCommentsList().add(comments);

//    	commentRepo.save(comments);

//        refreshComment();
		try {
			HistoryRequest historyRequest = new HistoryRequest();
			historyRequest.setId(generateId() + "-" + getAlphaNumericString(10));
			historyRequest.setStatus("Comment");
			historyRequest.setDateCreated(new Date());
			historyRequest.setKeterangan("Commented by " + userlogin);
			historyRequest.setCreatedBy(userlogin);
			historyRequest.setPosisi("Analyst");
			reqSelected.getHistoryRequestList().add(historyRequest);
		} catch (Exception e) {
		}
//        commentRepo.save(comments);
//    	System.out.println(reqSelected);
		reqSelected.setStage(2);
		reqSelected.setUserApproval(userlogin);
		reqSelected.setUpdateDate(new Date());
		reqSelected.setUpdateBy(userlogin);
		reqRepo.save(reqSelected);

		refreshComment();

		RequestContext.getCurrentInstance().update("req-content"); 
	}

	public void rejectData() {

		reqSelected.setUpdateBy(userlogin);
		reqSelected.setUpdateDate(new Date());
		if (reqSelected.getDecissionAnalyst().getId() == null) {
			DecissionAnalyst decissionAnalyst = new DecissionAnalyst();
			decissionAnalyst.setId(new Date().getTime() + "");
			reqSelected.setDecissionAnalyst(decissionAnalyst);
		}
//        String newRejectDesc = rejectReason.getKeterangan().replace("/[\\n,|]/gm", "<br>");
		try {
			HistoryRequest historyRequest = new HistoryRequest();
			historyRequest.setId(generateId() + "-" + getAlphaNumericString(10));
			historyRequest.setStatus("rejected by analis");
			historyRequest.setDateCreated(new Date());
			historyRequest.setKeterangan(rejectReason.getKeterangan());
			historyRequest.setCreatedBy(userlogin);
			reqSelected.getHistoryRequestList().add(historyRequest);
		} catch (Exception e) {
		}
		reqSelected.setStage(0);
		reqSelected.setRejectCode(rejectReason.getRjCode());
		reqSelected.setRejectDesc(rejectReason.getKeterangan());
		reqSelected.setDescReject(rejectReason.getKeterangan());
		reqSelected.setStatus("Rejected");
		showDetail = false;
		reqRepo.save(reqSelected);
		listRequest = new LazyDataModelJPA(reqRepo) {
			@Override
			protected long getDataSize() {
				return reqRepo.count(whereQuery());
			}

			@Override
			protected Page getDatas(PageRequest request) {
				return reqRepo.findAll(whereQuery(), request);
			}
		};
		RequestContext.getCurrentInstance().update("req-content");

	}

	public void assignData() {

		showDetail = false;
		if(nipAnalyst.isEmpty()) {
			 RequestContext.getCurrentInstance().execute("alert('Select Forward To User Assign');");
		}else {
			reqSelected.setUserApproval(nipAnalyst);
			
			System.out.println(">> "+reqSelected.getDecissionAnalyst());
			if (reqSelected.getDecissionAnalyst().getId() == null) {
				DecissionAnalyst decissionAnalyst = new DecissionAnalyst();
				decissionAnalyst.setId(new Date().getTime() + "");
				reqSelected.setDecissionAnalyst(decissionAnalyst);
			}
			reqSelected.setUpdateBy(userlogin);
			reqSelected.setUpdateDate(new Date());
			reqRepo.save(reqSelected);
			listRequest = new LazyDataModelJPA(reqRepo) {
				@Override
				protected long getDataSize() {
					return reqRepo.count(whereQuery());
				}

				@Override
				protected Page getDatas(PageRequest request) {
					return reqRepo.findAll(whereQuery(), request);
				}
			};
			RequestContext.getCurrentInstance().update("req-content");
		}
		
		
	}

	private Boolean assign;
	private String nipAnalyst;
	private String typeAssign;

	@Value("${path.ftp}")
	private String pathFtp;

	public void approveData() throws IOException, ParseException {
		System.out.println(reqSelected.getJenisLaporan());

//    Naik Limit
//    Perubahan Data
//    Perubahan Data CCBM
//    Reinstate 
		switch (reqSelected.getJenisLaporan()) {
		case "Naik Limit":
			runNaikLimit();
			break;
		case "Perubahan Data CCBM":
			runPerubahanDataCCBM();
			break;
		case "Reinstate":
			runReinstate();
			break;
		default:
			// code block
			RequestContext.getCurrentInstance().execute("alert('Maaf Belum Ada Jenis Laporan Tersedia');");
		}

	}

	private void runReinstate() {
		reqSelected.setUpdateBy(userlogin);
	    decision.setId(reqSelected.getReffId());
	    System.out.println("DESS2 : "+  decision.getFeeCode());
	    System.out.println("DESSS : "+ reqSelected.getDecissionAnalyst());
	    System.out.println("masuk sini " + reqSelected.getDataNadsSuspect());
	    reqSelected.setDecissionAnalyst(decision);
	    if(reqSelected.getDataNadsSuspect() != null) {
	    	 
			if(reqSelected.getDataNadsSuspect().getStatus().equals("HFP") || reqSelected.getDataNadsSuspect().getStatus().equals("Susfect") || reqSelected.getDataNadsSuspect().getStatus().equals("Under Investigation") || reqSelected.getDataNadsSuspect().getStatus().equals("Known Fraud")  ) {
	    		RequestContext.getCurrentInstance().execute("alert('Mohon Dilihat Status NADS ');");
	            return;
	    	}
	    }
    	
//        if(reqSelected.getProcode().equals("")||reqSelected.getProcode()==null){
//            RequestContext.getCurrentInstance().execute("alert('Mohon Isi Product Code Terlebih Dahulu');");
//            return;
//        }
	    
//	        try {
//	        	if(reqSelected.getDataNadsSuspect().getStatus().equals("HFP") || reqSelected.getDataNadsSuspect().getStatus().equals("Susfect") || reqSelected.getDataNadsSuspect().getStatus().equals("Under Investigation") || reqSelected.getDataNadsSuspect().getStatus().equals("Known Fraud")  ) {
//	        		RequestContext.getCurrentInstance().execute("alert('Mohon Dilihat Status NADS ');");
//	                return;
//	        	}
//	        	
//	            if(reqSelected.getProcode().equals("")||reqSelected.getProcode()==null){
//	                RequestContext.getCurrentInstance().execute("alert('Mohon Isi Product Code Terlebih Dahulu');");
//	                return;
//	            }
////	            if(reqSelected.getLimiitCc()<2000000){
////	                RequestContext.getCurrentInstance().execute("alert('Mohon Isi Limit Terlebih Dahulu');");
////	                return;
////	            }
//	        }catch (Exception e){
//	            RequestContext.getCurrentInstance().execute("alert('Mohon Lihat Kembali, Gagal!');");
//	            return;
//	        }
//	        if(reqSelected.getBillDay().equals("")){
//	            RequestContext.getCurrentInstance().execute("alert('Mohon Isi Bill Day Terlebih Dahulu');");
//	            return;
//	        }
//	        if(reqSelected.getDecissionAnalyst().getFeeCode().equals("")   ){
//	            RequestContext.getCurrentInstance().execute("alert('Mohon Isi Fee code Terlebih Dahulu');");
//	            return;
//	        }
	        if(reqSelected.getProcode().equals("120")){
	            if(reqSelected.getLimiitCc()<10000000){
	                RequestContext.getCurrentInstance().execute("alert('Limit tidak sesuai');");
	                return;
	            }
	        }else if(reqSelected.getProcode().equals("170")){
	            if(reqSelected.getLimiitCc()<20000000){
	                RequestContext.getCurrentInstance().execute("alert('Limit tidak sesuai');");
	                return;
	            }
	        }else if(reqSelected.getProcode().equals("110")){

	        }
	        reqSelected.setUpdateBy(userlogin);
	        reqSelected.setUpdateDate(new Date());
	       try{
	           if(reqSelected.getDecissionAnalyst().getPhoneCompany().length()>15){
	               reqSelected.getDecissionAnalyst().setPhoneCompany(reqSelected
	                       .getDecissionAnalyst().getPhoneCompany().substring(0,15));
	           }
	       }catch (Exception e){} 
	       
	       
	       if(reqSelected.getLimiitCc() == null || reqSelected.getLimiitCc().equals("") ){
	           RequestContext.getCurrentInstance().execute("alert('Mohon Isi Limit CC Terlebih Dahulu');");
	           return;
	       }
	        String cif = "CM"+new SimpleDateFormat("HHmmss").format(new Date());
	        HistoryRequest historyRequest = new HistoryRequest();
	        historyRequest.setId(generateId()+"-"+getAlphaNumericString(10));
	        historyRequest.setStatus("approved by analis");
	        historyRequest.setDateCreated(new Date());
	        historyRequest.setCreatedBy(userlogin);
	        reqSelected.getHistoryRequestList().add(historyRequest);
	        System.out.println(bwmk);
	        System.out.println(reqSelected.getLimiitCc());
	        if(bwmk<reqSelected.getLimiitCc()){
	        	RequestContext.getCurrentInstance().execute("alert('BWMK Lebih kecil daripada limit');");
	            reqRepo.save(reqSelected);
	            
	            approve=false;
	            assign=true;
	            RequestContext.getCurrentInstance().update("req-content");
	            return;
	        }
	        //flagging
	        reqSelected.setStage(0);
	        reqSelected.setStatus("Approved");
	        List<DedupDwh> dwhListAscend = new ArrayList<>();
	        List<DataRekening> dwhListRekening = new ArrayList<>(); 
	        reqSelected.setCif(cif); 
	        reqRepo.save(reqSelected);
	        showDetail=false;
	      RequestContext.getCurrentInstance().update("req-content");
	         
	        


	}

	private void runPerubahanDataCCBM() {
	 
   reqSelected.setUpdateBy(userlogin);
   decision.setId(reqSelected.getReffId());
   System.out.println("DESS2 : "+  decision.getFeeCode());
   System.out.println("DESSS : "+ reqSelected.getDecissionAnalyst());
   System.out.println("masuk sini ");
   reqSelected.setDecissionAnalyst(decision);
   
//       RequestContext.getCurrentInstance().execute("alert('"+reqSelected.getAlertDesc()+"');");
 
           try{
               if(reqSelected.getDataNadsSuspect().getStatus().equals("HFP") || reqSelected.getDataNadsSuspect().getStatus().equals("Susfect") || reqSelected.getDataNadsSuspect().getStatus().equals("Under Investigation") || reqSelected.getDataNadsSuspect().getStatus().equals("Known Fraud")  ) {
                   RequestContext.getCurrentInstance().execute("alert('Mohon Dilihat Status NADS ');");
                   return;
               } 
           }
           catch (Exception e){
               System.out.println(e);
           }

             

       reqSelected.setUpdateBy(userlogin);
       reqSelected.setUpdateDate(new Date());
      try{
          if(reqSelected.getDecissionAnalyst().getPhoneCompany().length()>15){
              reqSelected.getDecissionAnalyst().setPhoneCompany(reqSelected
                      .getDecissionAnalyst().getPhoneCompany().substring(0,15));
          }
      }catch (Exception e){} 

      
       String cif = "CM"+new SimpleDateFormat("HHmmss").format(new Date());
       HistoryRequest historyRequest = new HistoryRequest();
       historyRequest.setId(generateId()+"-"+getAlphaNumericString(10));
       historyRequest.setStatus("approved by analis");
       historyRequest.setDateCreated(new Date());
       historyRequest.setCreatedBy(userlogin);
       historyRequest.setCreatedBy(userlogin);
       reqSelected.getHistoryRequestList().add(historyRequest);
       System.out.println(bwmk);
       System.out.println(reqSelected.getLimiitCc());
//       if(bwmk<reqSelected.getLimiitCc()){
//       	RequestContext.getCurrentInstance().execute("alert('BWMK Lebih kecil daripada limit');");
//           reqRepo.save(reqSelected);
//
//           approve=false;
//           assign=true;
//           RequestContext.getCurrentInstance().update("req-content");
//           return;
//       }
       //flagging
       reqSelected.setStage(0);
       reqSelected.setStatus("Approved");
       List<DedupDwh> dwhListAscend = new ArrayList<>();
       List<DataRekening> dwhListRekening = new ArrayList<>(); 

       reqSelected.setCif(cif); 
      	     
       reqRepo.save(reqSelected);

       showDetail=false;
     RequestContext.getCurrentInstance().update("req-content");
     RequestContext.getCurrentInstance().execute("alert('Approve');");
 
	}

	private void runNaikLimit() {
		// TODO Auto-generated method stub

//  if(1==1){
//      System.out.println("Annual Salary : "+reqSelected.getSalaryAnnual() );
//      return;
//  }
		reqSelected.setUpdateBy(userlogin);
		decision.setId(reqSelected.getReffId());
		System.out.println("DESS2 : " + decision.getFeeCode());
		System.out.println("DESSS : " + reqSelected.getDecissionAnalyst());
		System.out.println("masuk sini ");
		reqSelected.setDecissionAnalyst(decision);

//
//if (reqSelected.getSalaryAnnual().equals("null") || !reqSelected.getDecissionAnalyst().getSalaryTemp().equals("")){
//   reqSelected.setSalaryAnnual(reqSelected.getDecissionAnalyst().getSalaryTemp());
//}
//
//if (reqSelected.getNpwp().equals("null") || !reqSelected.)

		TmMsmile gestunCheck = tmMsmileApiRepo.findFirstByReffIdAndTypeCase(reqSelected.getReffId(), "97");

//   try{
//       if (gestunCheck != null){
//           if(!gestunCheck.getFlagRpa().equals("2")){
//               RequestContext.getCurrentInstance().execute("alert('Data Gestun belum diproses');");
//               return;
//           }
//       }
//
//   }catch (Exception e){
//       e.printStackTrace();
//   }

		RequestContext.getCurrentInstance().execute("alert('" + reqSelected.getAlertDesc() + "');");

		try {
			try {
				if (reqSelected.getDataNadsSuspect().getStatus().equals("HFP")
						|| reqSelected.getDataNadsSuspect().getStatus().equals("Susfect")
						|| reqSelected.getDataNadsSuspect().getStatus().equals("Under Investigation")
						|| reqSelected.getDataNadsSuspect().getStatus().equals("Known Fraud")) {
					RequestContext.getCurrentInstance().execute("alert('Mohon Dilihat Status NADS ');");
					return;
				}

				if (reqSelected.getProcode().equals("") || reqSelected.getProcode() == null) {
					RequestContext.getCurrentInstance().execute("alert('Mohon Isi Product Code Terlebih Dahulu');");
					return;
				}
			} catch (Exception e) {
				System.out.println(e);
			}

			System.out.println("LIMIT CC :" + reqSelected.getLimiitCc());
			if (reqSelected.getLimiitCc() < 2000000) {
				RequestContext.getCurrentInstance().execute("alert('Mohon Isi Limit Terlebih Dahulu');");
				return;
			}
		} catch (Exception e) {
			System.out.println("LIMIT CC :" + reqSelected.getLimiitCc());
			RequestContext.getCurrentInstance().execute("alert('Mohon Isi Limit Terlebih Dahulu');");
			return;
		}
//   try{
//       if(reqSelected.getBillDay().equals("")){
//           RequestContext.getCurrentInstance().execute("alert('Mohon Isi Bill Day Terlebih Dahulu');");
//           return;
//       }
//   }catch (Exception e){
//       System.out.println(e);
//   }

//   if(reqSelected.getDecissionAnalyst().getFeeCode().equals("")   ){
//       RequestContext.getCurrentInstance().execute("alert('Mohon Isi Fee code Terlebih Dahulu');");
//       return;
//   }
		try {
			if (reqSelected.getProcode().equals("120")) {
				if (reqSelected.getLimiitCc() < 10000000) {
					RequestContext.getCurrentInstance().execute("alert('Limit tidak sesuai');");
					return;
				}
			} else if (reqSelected.getProcode().equals("170")) {
				if (reqSelected.getLimiitCc() < 20000000) {
					RequestContext.getCurrentInstance().execute("alert('Limit tidak sesuai');");
					return;
				}
			} else if (reqSelected.getProcode().equals("110")) {

			}
		} catch (Exception e) {

		}

		reqSelected.setUpdateBy(userlogin);
		reqSelected.setUpdateDate(new Date());
		try {
			if (reqSelected.getDecissionAnalyst().getPhoneCompany().length() > 15) {
				reqSelected.getDecissionAnalyst()
						.setPhoneCompany(reqSelected.getDecissionAnalyst().getPhoneCompany().substring(0, 15));
			}
		} catch (Exception e) {
		}
//   saveAuditTrail("Approved ",userlogin,"Analyst",reqSelected.getReffId());

		if (reqSelected.getLimiitCc() == null || reqSelected.getLimiitCc().equals("")) {
			RequestContext.getCurrentInstance().execute("alert('Mohon Isi Limit CC Terlebih Dahulu');");
			return;
		}
		String cif = "CM" + new SimpleDateFormat("HHmmss").format(new Date());
		HistoryRequest historyRequest = new HistoryRequest();
		historyRequest.setId(generateId() + "-" + getAlphaNumericString(10));
		historyRequest.setStatus("approved by analis");
		historyRequest.setDateCreated(new Date());
		historyRequest.setCreatedBy(userlogin);
		historyRequest.setCreatedBy(userlogin);
		reqSelected.getHistoryRequestList().add(historyRequest);
		System.out.println(bwmk);
		System.out.println(reqSelected.getLimiitCc());
		if (bwmk < reqSelected.getLimiitCc()) {
			RequestContext.getCurrentInstance().execute("alert('BWMK Lebih kecil daripada limit');");
			reqRepo.save(reqSelected);

			approve = false;
			assign = true;
			RequestContext.getCurrentInstance().update("req-content");
			return;
		}
		// flagging
		reqSelected.setStage(0);
		reqSelected.setStatus("Approved");
		List<DedupDwh> dwhListAscend = new ArrayList<>();
		List<DataRekening> dwhListRekening = new ArrayList<>();
//   if(reqSelected.getDedupDwhs().size()>0){
//       dwhListAscend = reqSelected.getDedupDwhs().stream().filter(a -> a.getCif().length()>2).collect(Collectors.toList());
//   }else if(reqSelected.getDataRekening().size()>0){
//       dwhListRekening = reqSelected.getDataRekening().stream()
//               .filter(a -> a.getCif().length()>2).collect(Collectors.toList());
//   }
//   if(dwhListAscend.size()>0){
//       cif=dwhListAscend.get(0).getCif();
//   }else if(dwhListRekening.size()>0){
//       cif=dwhListRekening.get(0).getCif();
//   }else{
//       if(reqSelected.getDecissionAnalyst().getCif()==null
//               || reqSelected.getDecissionAnalyst().getCif().equals("")){
//           cif = "CM"+new SimpleDateFormat("HHmmss").format(new Date());
//       }else{
//           cif = reqSelected.getDecissionAnalyst().getCif();
//       }
//   }
//   saveAuditTrail("Approved ",userlogin,"Analyst",reqSelected.getReffId());

		reqSelected.setCif(cif);
//   if(reqSelected.getInstantIssuance()){
//       SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//       try{
//           reqSelected.setRegionalId(lokasiCetakRepo.findAllByInstantTrue().get(0).getId());
//       }catch (Exception e){
//
//       }
//       reqSelected.setNameFileAscend("APLIIC"+reqSelected.getRegionalId()+"." +sdf.format(new Date())+"0");
////       generateFile(reqSelected);
//       System.out.println(reqSelected.getTypeRequest());
//       if(reqSelected.getTypeRequest().equals("S")){
//           generateFileSupplement(reqSelected);
//       }else{
//           generateFile(reqSelected);
//       }
//   }
		TmMsmile msmile = new TmMsmile();
		msmile.setCardNumber(reqSelected.getCardNo());

		String sqlMob = "SELECT * FROM dbo.TBL_VW_NADS WHERE CARD_NUMBER = '" + reqSelected.getCardNo() + "'";
		Connection con = getKoneksiDwh();
		PreparedStatement ps;
		ResultSet rsMob;
		try {
			ps = con.prepareStatement(sqlMob);
			rsMob = ps.executeQuery();
			if (!rsMob.next()) {
				System.out.println("data not found");
			} else {
				do {
					BigInteger bigInteger = BigDecimal.valueOf(Double.parseDouble(rsMob.getString("CREDIT_LIMIT")))
							.toBigInteger();
					if (reqSelected.getTypeNaikLimit() != null) {
						if (reqSelected.getTypeNaikLimit().equals("temporary")) {
							msmile.setKodeposRmh(bigInteger.toString());
						} else {
							msmile.setLimitAwal(bigInteger.toString());
						}
					}

				} while (rsMob.next());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		BigInteger limitBigInteger = BigDecimal.valueOf(reqSelected.getLimiitCc()).toBigInteger();
		msmile.setLimit(String.valueOf(limitBigInteger));

		Double limitCc = reqSelected.getLimiitCc();
		Double limitCalculate = limitCc * 0.25;
		System.out.println("reqSelected.getLimiitCc() : " + limitCalculate);
		BigInteger limitResultBigInteger = BigDecimal.valueOf(limitCalculate).toBigInteger();
		if (reqSelected.getTypeNaikLimit() != null) {
			if (!reqSelected.getTypeNaikLimit().equals("temporary")) {
				msmile.setLimitResult(String.valueOf(limitResultBigInteger));
			}
		}

		msmile.setFlagRpa("1");

		msmile.setReffId(reqSelected.getReffId());

		msmile.setTypeCase("48");

		String newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		msmile.setCreateDate(newDate);
		if (reqSelected.getTypeNaikLimit() != null) {
			if (reqSelected.getTypeNaikLimit().equals("permanent")) {
				msmile.setCashLimitCustomer(String.valueOf(limitResultBigInteger));
			}
		}
		msmile.setCustomerNumber(reqSelected.getCustNumber());
		msmile.setNpwp(reqSelected.getNpwp());
		if (reqSelected.getSalaryAnnual().equals(null)) {
			msmile.setAnnualSalary(reqSelected.getDecissionAnalyst().getSalaryTemp());
		} else {
			msmile.setAnnualSalary(reqSelected.getSalaryAnnual());
		}
		if (reqSelected.getTypeNaikLimit() != null) {
			if (reqSelected.getTypeNaikLimit().equals("temporary")) {
				msmile.setLimit(String.valueOf(BigDecimal.valueOf(reqSelected.getLimiitCc()).toBigInteger()));
				if (reqSelected.getUpdateDate() != null) {
					String effektifDate = new SimpleDateFormat("ddMMyyyy").format(reqSelected.getUpdateDate());
					// tanggal efektif
					msmile.setLimitAwal(effektifDate);
				}
				System.out.println("TENOR ? " + reqSelected.getTenor());
				if ((reqSelected.getTenor() != null) && (reqSelected.getTenor() != "")) {
					msmile.setTenor(reqSelected.getTenor());
					Date endDate = DateUtils.addMonths(reqSelected.getUpdateDate(),
							Integer.parseInt(reqSelected.getTenor()));
					// tanggal berakhir
					msmile.setLimitResult(new SimpleDateFormat("ddMMyyyy").format(endDate));
				}

				msmile.setTypeCase("53");
			}
		}
		tmMsmileApiRepo.save(msmile);
		reqRepo.save(reqSelected);

		showDetail = false;
		RequestContext.getCurrentInstance().update("req-content");
		RequestContext.getCurrentInstance().execute("alert('Approve');");

//   listRequest = new LazyDataModelJPA(reqRepo) {
//       @Override
//       protected long getDataSize() {
//           return reqRepo.count(whereQuery());
//       }
//
//       @Override
//       protected Page getDatas(PageRequest request) {
//           return reqRepo.findAll(whereQuery(), request);
//       }
//   };
//

//   try {
//       if(reqSelected.getSendSms()){
//           String message="Nasabah Yth, pengajuan Kartu Kredit Anda telah disetujui dan akan segera kami kirim. Utk info lebih lanjut hubungi MegaCall di 1500010 (HP)";
//
//           RestTemplate restTemplate = new RestTemplate();
//           String sms= "http://10.14.18.177:8089/notif?nohandphone="+convertPhoneNumber(reqSelected.getPhone())
//                   +"&sms=" + message
//                   +"&ref_transaction="+new Date().getTime()+"&engine=DETIKCREDIT";
//           System.out.println(sms);
//           ResponseEntity<String> responseEntity = restTemplate.getForEntity(sms,String.class);
//           System.out.println(responseEntity.getBody());
//       }
//       try {
//           if(reqSelected.getPrivyStatus()){
//               callPrivy(reqSelected);
//           }
//       }catch (Exception e){}
//       showDetail=false;
//       RequestContext.getCurrentInstance().update("req-content");
//   }catch (Exception e){
//       RequestContext.getCurrentInstance().execute("alert('Harap Pilih Send Sms');");
//   }

	}

	// @Autowired
//    private DataSpotRepo dataSpotRepo;
	public void callSpotCheck() {
//        if(reqSelected.getTypeSpotCheck().equals("") || reqSelected.getTypeSpotCheck()==null){
//            RequestContext.getCurrentInstance().execute("alert('Please Pilih Type Spot check')");
//            return;
//        }
//        saveAuditTrail("Assign To Spotchec ",userlogin,"Analyst",reqSelected.getReffId());
//        DataSpotCheck data = new DataSpotCheck();
//        reqSelected.setLastUser(userlogin);
		reqSelected.setUpdateBy(userlogin);
//        reqSelected.setDtUserSpotcheck(new Date());
//        reqSelected.setUpdateDate(new Date());
//        if(reqSelected.getTypeSpotCheck().equals("01-Home")
//                ||reqSelected.getTypeSpotCheck().equals("03-Home And Office")){
//            data = new DataSpotCheck();
//            data.setDateCreated(new Date());
//            data.setRegional(reqSelected.getRegional());
//            data.setId(reqSelected.getReffId()+"_r");
//            reqSelected.setSpotchechHome(true);
//            try {
//                data.setAlamat(reqSelected.getDecissionAnalyst().getAlamat()+","+reqSelected
//                        .getDecissionAnalyst().getAlamat2());
//            }catch (Exception e){
//                data.setAlamat(reqSelected.getDecissionAnalyst().getAlamat());
//            }
//            data.setAssign(false);
//            data.setHp(reqSelected.getPhone());
//            data.setStatus(false);
//            data.setJenisKelamin(reqSelected.getDecissionAnalyst().getJenisKelamin());
//            data.setKecamatan(reqSelected.getDecissionAnalyst().getKecamtan());
//            data.setKelurahan(reqSelected.getDecissionAnalyst().getKelurahan());
//            data.setKodepos(reqSelected.getDecissionAnalyst().getKodepos());
//            data.setKota(reqSelected.getDecissionAnalyst().getKota());
//            data.setMomName(reqSelected.getDecissionAnalyst().getMomName());
//            data.setNamaLengkap(reqSelected.getCustName());
//            data.setNik(reqSelected.getCustId());
//            data.setReffId(reqSelected.getReffId());
//            data.setRt(reqSelected.getDecissionAnalyst().getRt());
//            data.setRw(reqSelected.getDecissionAnalyst().getRw());
//            data.setSla(0);
//            data.setTanggalLahir(reqSelected.getDob());
//            data.setTmptLahir(reqSelected.getDukcapil().getTmptLhr());
//            data.setType("Home");
//            data.setTypeSpot("Home");
//            dataSpotRepo.save(data);
//        }
//        if(reqSelected.getTypeSpotCheck().equals("02-Office")
//                ||reqSelected.getTypeSpotCheck().equals("03-Home And Office")){
//            data = new DataSpotCheck();
//            data.setRegional(reqSelected.getRegional());
//            data.setPerusahaan(reqSelected.getDecissionAnalyst().getPerusahaan());
//            reqSelected.setSpotchechOffice(true);
//            data.setDateCreated(new Date());
//            data.setId(reqSelected.getReffId()+"_k");
//           try{
//               data.setAlamat(reqSelected.getDecissionAnalyst().getAddrCompany()+","+reqSelected
//                       .getDecissionAnalyst().getAddrCompany2());
//           }catch (Exception e){
//               data.setAlamat(reqSelected.getDecissionAnalyst().getAddrCompany());
//           }
//            data.setAssign(false);
//            data.setHp(reqSelected.getPhone());
//            data.setJenisKelamin(reqSelected.getDecissionAnalyst().getJenisKelamin());
//            data.setKecamatan(reqSelected.getDecissionAnalyst().getKecCompany());
//            data.setKelurahan(reqSelected.getDecissionAnalyst().getKelCompay());
//            data.setKodepos(reqSelected.getDecissionAnalyst().getKodePosCompany());
//            data.setKota(reqSelected.getDecissionAnalyst().getKotaCompany());
//            data.setMomName(reqSelected.getDecissionAnalyst().getMomName());
//            data.setNamaLengkap(reqSelected.getCustName());
//            data.setNik(reqSelected.getCustId());
//            data.setReffId(reqSelected.getReffId());
//            data.setSla(0);
//            data.setTanggalLahir(reqSelected.getDob());
//            data.setTmptLahir(reqSelected.getDukcapil().getTmptLhr());
//            data.setType("Office");
//            data.setTypeSpot("Office");
//            data.setStatus(false);
//            dataSpotRepo.save(data);
//        }
		reqSelected.setStage(4);
		reqRepo.save(reqSelected);

		approve = false;
		showDetail = false;
		HistoryRequest historyRequest = new HistoryRequest();
		historyRequest.setId(generateId() + "-" + getAlphaNumericString(10));
		historyRequest.setStatus("Assig to Spotcheck");
		historyRequest.setDateCreated(new Date());
		historyRequest.setCreatedBy(userlogin);
		reqSelected.getHistoryRequestList().add(historyRequest);
		listRequest = new LazyDataModelJPA(reqRepo) {
			@Override
			protected long getDataSize() {
				return reqRepo.count(whereQuery());
			}

			@Override
			protected Page getDatas(PageRequest request) {
				return reqRepo.findAll(whereQuery(), request);
			}
		};
		RequestContext.getCurrentInstance().update("req-content");
	}

	public void phonever() {
//        saveAuditTrail("Assign To Phonefer ",userlogin,"Analyst",reqSelected.getReffId());
//        try{
//            Comments commenta = new Comments();
//            commenta.setId(generateId()+"-"+getAlphaNumericString(10));
//            commenta.setDateCreated(new Date());
//            commenta.setUserId(userlogin);
//            commenta.setComments(reqSelected.getDecissionAnalyst().getCatatan());
//            reqSelected.getCommentsList().add(commenta);
//        }catch (Exception e){
//
//        }
//        if(String.valueOf(reqSelected.getUserPhonever()).equals("null")){
//            reqSelected.setStage(7);
//        }else{
//            reqSelected.setStage(3);
//            reqSelected.setDtUserAdmPhone(new Date());
//        }
//        reqSelected.setLastUser(userlogin);
		reqSelected.setUpdateBy(userlogin);
		reqSelected.setUpdateDate(new Date());
		approve = false;
		HistoryRequest historyRequest = new HistoryRequest();
		historyRequest.setId(generateId() + "-" + getAlphaNumericString(10));
		historyRequest.setStatus("Assig to Phonever");
		historyRequest.setDateCreated(new Date());
		historyRequest.setCreatedBy(userlogin);
		reqSelected.getHistoryRequestList().add(historyRequest);

		reqRepo.save(reqSelected);
		showDetail = false;
		listRequest = new LazyDataModelJPA(reqRepo) {
			@Override
			protected long getDataSize() {
				return reqRepo.count(whereQuery());
			}

			@Override
			protected Page getDatas(PageRequest request) {
				return reqRepo.findAll(whereQuery(), request);
			}
		};
		RequestContext.getCurrentInstance().update("req-content");
	}

	public void imagePdf(String id) {
		pdfSlik = id;
		RequestContext.getCurrentInstance().openDialog("idebDialog", getDialogOptions(), null);
	}

	public Map<String, Object> getDialogOptions() {
		Map<String, Object> options = new HashMap<>();
		options.put("resizable", false);
		options.put("draggable", true);
		options.put("modal", true);
		options.put("height", 400);
		options.put("contentHeight", "100%");
		return options;
	}

	public String convertCurrency(BigDecimal bigDecimal) {
		String numberAsString = "";
		try {
			NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
			numberAsString = numberFormat.format(bigDecimal);
		} catch (Exception e) {
			 
		}
		return numberAsString;
	}
	 

	public void recalculate() {
//        if(listSelectIdebPefindo.size()==0){
//            RequestContext.getCurrentInstance().execute("alert('Harap Pilih terlebih dahulu');");
//            return;
//        }
//        List<IndividualRecords> listIndividual = new ArrayList<>();
		List<String> listmustFilter = new ArrayList<>();
		listmustFilter.add("NotSpecified");
		listmustFilter.add("NoNegativeStatus");
		BigDecimal totalOutstandingCc = new BigDecimal(0);
		BigDecimal totalOutstandingNonCc = new BigDecimal(0);
		BigDecimal totalLimitCc = new BigDecimal(0);
		BigDecimal totalLimitNonCc = new BigDecimal(0);
		BigDecimal averageLimit = new BigDecimal(0);
		BigDecimal ccUtilization = new BigDecimal(0);
		BigDecimal nonCcRemaining = new BigDecimal(0);
		BigDecimal maxCc = new BigDecimal(0);
		BigDecimal maxLoan = new BigDecimal(0);
		Integer worstDpd = 0;
		Integer diffMonthNewest = 0;
		Integer diffMonthOldest = 0;
		long worsCollect = 0;
		Integer worstDpd30 = 0;
		Integer worstDpd60 = 0;
		Calendar oldestCalFac = null;
		Calendar newestFac = null;
		Integer maxCol2 = 0;
		Integer totalCcActive = 0;
		Connection con = getKoneksiPrewash();

		reqRepo.save(reqSelected);
//        con.close();
		RequestContext.getCurrentInstance().update("req-content");
//        if(reqSelected.getAgregrasiPefindo().getMobOldest()>=12){
//            reqSelected.setTypeDebitur("CFC");
//        }else{
//            if(reqSelected.getAgregrasiPefindo().getTotalCcActive()>0){
//                reqSelected.setTypeDebitur("RFCa");
//            }else{
//                List<Contract> listContract = reqSelected.getDataPefindo().getListIndividualRecords()
//                        .get(0).getReportDokumenPefindo().getListContracts();
//                if(listContract.stream().filter(a->a.getPhaseOfContract().equals("Open"))
//                        .count()>0){
//                    reqSelected.setTypeDebitur("RFCb");
//                }else{
//                    reqSelected.setTypeDebitur("RFCc");
//                }
//            }
//        }
		RestTemplate restTemplate2 = new RestTemplate();

		try {
			ResponseEntity<BigDecimal> result = restTemplate2
					.getForEntity("http://10.14.19.72:8300/api/income/" + reqSelected.getReffId(), BigDecimal.class);
			BigDecimal deriveIncomeAkki = result.getBody();
//           reqSelected.setSurrogateincome(deriveIncomeAkki);
			reqSelected.setSalaryMonth(deriveIncomeAkki.doubleValue());
		} catch (Exception e) {

		}
		reqRepo.save(reqSelected);
		if (reqSelected.getStage() == 0) {
			RequestContext.getCurrentInstance().execute("alert('Applicant Auto Reject')");
			String message = "Mohon maaf, kami belum dapat memenuhi permohonan Kartu Kredit Anda pada saat ini. Utk info lebih lanjut hubungi MegaCall di 1500010 (HP). Terimakasih";
			RestTemplate restTemplate = new RestTemplate();
			String sms = "http://10.14.18.177:8089/notif?nohandphone=" + convertPhoneNumber(reqSelected.getPhone())
					+ "&sms=" + message + "&ref_transaction=" + new Date().getTime() + "&engine=DETIKCREDIT";
//            ResponseEntity<String> responseEntity = restTemplate.getForEntity(sms,String.class);
//            System.out.println(responseEntity.getBody());
			showDetail = false;
			RequestContext.getCurrentInstance().update("req-content");
		}
	}

	private Specification<Request> whereQuery() {
		List<Predicate> predicates = new ArrayList<>();
		return new Specification<Request>() {
			@Override
			public Predicate toPredicate(Root<Request> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
				predicates.add(cb.equal(root.<Integer>get("status"), "On Process"));
				predicates.add(cb.notLike(cb.lower(root.<String>get("reffId")), getLikePattern("SUP")));
				predicates.add(cb.equal(root.<Integer>get("stage"), 2));
				 if( getCurrentUser().getType().equals("TMA")) {
					 predicates.add(cb.equal(root.<Integer>get("jenisLaporan"), "Perubahan Data CCBM"));
				 }else {
					 predicates.add(cb.equal(root.<Integer>get("userApproval"), userlogin));
				 }
				
				
				if (StringUtils.isNotBlank(value)) {
					switch (field) {
					case "reffId":
						predicates.add(cb.equal(root.<Integer>get(field), value));
						break;
					default:
						predicates.add(cb.like(cb.lower(root.<String>get(field)), getLikePattern(value.trim())));
						break;
					}
				}
				cq.orderBy(cb.desc(root.get("datecreated")));
				return andTogether(predicates, cb);
			}
		};
	}
	
	public String convertStringToDouble(Object a) {
		return "";
		
	}
	
	private String getLikePattern(String searchTerm) {
		return new StringBuilder("%").append(searchTerm.toLowerCase().replaceAll("\\*", "%")).append("%").toString();
	}

	private Predicate andTogether(List<Predicate> predicates, CriteriaBuilder cb) {
		return cb.and(predicates.toArray(new Predicate[0]));
	}

	public void resetFilter() {
		field = "";
		value = "";
		listRequest = new LazyDataModelJPA(reqRepo) {
			@Override
			protected long getDataSize() {
				return reqRepo.count(whereQuery());
			}

			@Override
			protected Page getDatas(PageRequest request) {
				return reqRepo.findAll(whereQuery(), request);
			}
		};
	}

	public void calculateDbr() {
		try {
			reqSelected.setSalaryMonth(salary);
			ajaxdata();
		} catch (Exception e) {
			reqSelected.setSalaryMonth(0.0);
		}
		if (periodeSpt == null && reqSelected.getTypeIncome().equals("SPT")) {
			RequestContext.getCurrentInstance().execute("alert('Harap Isi terlebih dahulu Jumlah Periode');");
		} else {
//            System.out.println(angkaD+"----"+reqSelected.getTypeIncome());
//            reqSelected=calculateDbrLib(reqSelected,angkaD,periodeSpt);
			if (reqSelected.getDbr().intValue() > 35) {
				out = "DBR Kurang dari ketentuan -> " + reqSelected.getDbr();
			} else {
				out = "DBR => " + reqSelected.getDbr() + " %";
			}
			angkaD = reqSelected.getSalaryMonth();
		}
	}

	public void handleFileUpload(FileUploadEvent event) {
		FacesMessage msg = new FacesMessage("Successful", event.getFile().getFileName() + " is uploaded.");
		FacesContext.getCurrentInstance().addMessage(null, msg);
		try {
			FacesContext aFacesContext = FacesContext.getCurrentInstance();
			ServletContext context = (ServletContext) aFacesContext.getExternalContext().getContext();
			String realPath = context.getRealPath("/resources/" + reqSelected.getPathFile() + "/");
			byte[] arquivo = event.getFile().getContents();
			String nameFile = realPath + event.getFile().getFileName();
			File file = new File(realPath);
			// Creating the directory
			boolean bool = file.mkdirs();
			FileOutputStream fos = new FileOutputStream(nameFile);
			fos.write(arquivo);
			FileLocation fileLocation = new FileLocation();
			fileLocation.setId(getAlphaNumericString(12));
			fileLocation.setLocation(reqSelected.getPathFile() + "/" + event.getFile().getFileName());
			fileLocation.setFilename(event.getFile().getFileName());
			fileLocation.setRefId(reqSelected.getReffId());
			fileLocation.setType("other");
			reqSelected.getListFile().add(fileLocation);
			reqRepo.save(reqSelected);
			imagesLain = new ArrayList<>();
			String[] arrTemp = new String[] { "selfie.jpeg", "ktp.jpeg" };
			List<String> notIn = Arrays.asList(arrTemp);
			for (FileLocation fileLocation2 : reqSelected.getListFile()) {
				if (!notIn.contains(fileLocation2.getFilename())) {
					imagesLain.add(fileLocation2.getLocation());
				}
			}

			RequestContext.getCurrentInstance().update("req-content");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getDataFromDukcapil() {
//        reqSelected = requestDataDukcapil(reqSelected,reqSelected.getCustId());
		if (!reqSelected.getDataProcess()) {
			RequestContext.getCurrentInstance().execute("alert('Nik Tidak Ditemukan Di Dukcapil');");
			return;
		} else {
//            reqSelected.setCustId(ktp);
			reqRepo.save(reqSelected);
		}
		RequestContext.getCurrentInstance().update("req-content");
	}

	@Value("${spring.datasource.url}")
	String url;

	@Value("${spring.datasource.username}")
	String username;

	@Value("${spring.datasource.password}")
	String password;

	public void rerunData() throws IOException, ParseException {
		reqSelected.setUpdateBy(userlogin);
		decision.setId(reqSelected.getReffId());
		System.out.println("masuk sini ");

		reqSelected.setUpdateBy(userlogin);
		reqSelected.setUpdateDate(new Date());

		String cif = "CM" + new SimpleDateFormat("HHmmss").format(new Date());
 
        //insert to history 
        HistoryRequest reqhis = new HistoryRequest();
        reqhis.setId(UUID.randomUUID().toString());   
		reqhis.setStatus("Rerun");
		reqhis.setDateCreated(new Date());
		reqhis.setCreatedBy(userlogin);
		reqhis.setKeterangan("");
		reqhis.setPosisi(typelogin);
		reqhis.setRefId(reqSelected.getReffId().trim());
		 
		
		
		 
		reqSelected.getHistoryRequestList().add(reqhis);

		if (bwmk < reqSelected.getLimiitCc()) {
			RequestContext.getCurrentInstance().execute("alert('BWMK Lebih kecil daripada limit');");
			reqRepo.save(reqSelected);

			approve = false;
			assign = true;
			RequestContext.getCurrentInstance().update("req-content");
			return;
		}

		System.out.println("masuk sini");
		try {
			// create a java mysql database connection
			String myDriver = "com.mysql.jdbc.Driver";
			String myUrl = url;
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(myUrl, username, password);

			// create the java mysql update preparedstatement
			String query = "update data_instate SET flag_procced = 1 WHERE dok_tl =?";
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, reqSelected.getNumerator().trim());

			// execute the java preparedstatement
			preparedStmt.executeUpdate();

			conn.close();
		} catch (Exception e) {
			System.err.println("Got an exception! ");
			System.err.println(e.getMessage());
		}

		List<DedupDwh> dwhListAscend = new ArrayList<>();
		List<DataRekening> dwhListRekening = new ArrayList<>();
		reqSelected.setStatus("Rerun");
		reqSelected.setCif(cif);

		reqRepo.save(reqSelected);
		showDetail = false;
		RequestContext.getCurrentInstance().update("req-content");

	}

	public String convertMOB(String param) {

		System.out.println("param " + param);

		Calendar c = Calendar.getInstance();
		Date d = null;
		try {
			d = new SimpleDateFormat("yyyyMMdd").parse(param);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		c.setTime(d);

		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH) + 1;
		int date = c.get(Calendar.DATE);
		LocalDate mob1 = LocalDate.of(year, month, date);
		LocalDate now1 = LocalDate.now();
		Period MOB = Period.between(mob1, now1);
		String MOBTOSTRING = MOB.getYears() + " tahun " + MOB.getMonths() + " bulan " + MOB.getDays() + " hari";
		return MOBTOSTRING;

	}

	protected Connection getKoneksiDwh() {
		Connection con = null;
		try {
			Class.forName("net.sourceforge.jtds.jdbc.Driver");
			con = DriverManager.getConnection(dbUrlRek, dbUserRek, dbPassRek);
		} catch (Exception e) {

		}
		return con;
	}
}
