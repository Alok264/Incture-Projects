package com.imo.workorder.operationTime.service.Impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.commons.io.IOUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.imo.workorder.operationTime.dto.OpsdetailsDto;
import com.imo.workorder.operationTime.dto.WorkOpsTimeDetails;
import com.imo.workorder.operationTime.dto.WorkOrderDmsResponseDto;
import com.imo.workorder.operationTime.dto.Workorderopsdto;
import com.imo.workorder.operationTime.dto.workOrderOpsUpdate;
import com.imo.workorder.operationTime.model.WorkOrderDms;
import com.imo.workorder.operationTime.model.WorkOrderOpsModel;
import com.imo.workorder.operationTime.repo.WorkOrderDmsRepo;
import com.imo.workorder.operationTime.repo.WorkOrderOpsRepo;
import com.imo.workorder.operationTime.service.WorkOrderOpsService;
import com.imo.workorder.operationTime.util.WorkOrderDmsConstants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WorkOrderOpsServiceImpl implements WorkOrderOpsService {

	@Autowired
	private WorkOrderOpsRepo opsrepo;

	@Autowired
	ModelMapper modelMapper;

	@Override
	public ResponseEntity<?> SaveWorkOrderOps(Workorderopsdto details) {

		int opscount = opsrepo.getopscount(details.getPlant(), details.getWorkorder(), details.getOperation());
		// check if record exists
		if (opscount == 0) {
			// insert a record if record doesn't exist
			WorkOrderOpsModel data = new WorkOrderOpsModel();
			data = modelMapper.map(details, WorkOrderOpsModel.class);
			opsrepo.save(data);

		} else {
			// get the data object if it exists
			WorkOrderOpsModel previousData = opsrepo.getopsData(details.getPlant(), details.getWorkorder(),
					details.getOperation());

			if (previousData.getStatus().equalsIgnoreCase("Completed")) {
				// check if the operation is already completed, if completed no update
				return new ResponseEntity<>("Operation is already completed", HttpStatus.OK);

			} else {
				if (details.getStatus().equalsIgnoreCase("Onhold")) {
					// change the status and endtime -> pause start time
					if (previousData.getEndTime() == null) {
						opsrepo.updateopsOnhold(details.getPlant(), details.getWorkorder(), details.getOperation(),
								details.getEndTime(), details.getStatus());
					}

				} else if (details.getStatus().equalsIgnoreCase("Running")) {
					double previous_duration = opsrepo.getpreviouspauseduration(details.getPlant(),
							details.getWorkorder(), details.getOperation());

					if (previousData.getEndTime() != null) {
						long currentDutration = getMinsDifference(previousData.getEndTime(), details.getEndTime());
						double totalduration = previous_duration + currentDutration;
						// update the status, pause_duration after summing up previous duration data and
						// set endtime as null
						opsrepo.updateopsOnrestart(details.getPlant(), details.getWorkorder(), details.getOperation(),
								null, details.getStatus(), totalduration);
					}

				} else {
					// if status is completed, update endtime and status field
					opsrepo.updateopsOncomplete(details.getPlant(), details.getWorkorder(), details.getOperation(),
							details.getEndTime(), details.getStatus());
				}
			}
		}
		return new ResponseEntity<>(details, HttpStatus.CREATED);
	}

	public long getMinsDifference(Timestamp timestamp1, Timestamp timestamp2) {
		// Calculate the duration between two Timestamps
		Duration duration = Duration.between(timestamp1.toInstant(), timestamp2.toInstant());
		// Get the difference in hours
		long minsDifference = duration.toMinutes();
		System.out.println("OEE data[hours difference]:" + minsDifference);
		return minsDifference;
	}

	public double getWorkOrderOpsTime(String plant, String workorder, String ops) {
		WorkOrderOpsModel opsData = opsrepo.getopsData(plant, workorder, ops);
		System.out.println("ops data: " + opsData);
		double actual_work = (getMinsDifference(opsData.getStartTime(), opsData.getEndTime())
				- opsData.getPause_duration()) / 60.00;
//		return new ResponseEntity<>(Math.round(actual_work * 100.0) / 100.0, HttpStatus.OK);
		return Math.round(actual_work * 100.0) / 100.0;
	}

	@Override
	public ResponseEntity<?> getWorkOrderOps(String plant, String workorder, String ops) {
		WorkOrderOpsModel opsData = opsrepo.getopsData(plant, workorder, ops);
		OpsdetailsDto response = new OpsdetailsDto();
		if (opsData == null) {
			return new ResponseEntity<>(response, HttpStatus.OK);
		}
		response.setOpsModel(opsData);
		if (opsData.getStatus().equalsIgnoreCase("Running")) {
			response.setActualtime(0);
		} else {
			response.setActualtime(getWorkOrderOpsTime(plant, workorder, ops));
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<?> UpdateWorkOrderOps(workOrderOpsUpdate payload) {
		for (WorkOpsTimeDetails data : payload.getTimeData()) {
			int opscount = opsrepo.getopscount(payload.getPlant(), payload.getWorkorder(), data.getOperation());
			// check if record exists
			if (opscount == 0) {
				Workorderopsdto details = new Workorderopsdto();
				details.setEndTime(data.getEndTime());
				details.setOperation(data.getOperation());
				details.setPause_duration(opscount);
				details.setPlant(payload.getPlant());
				details.setStartTime(data.getStartTime());
				details.setStatus(data.getStatus());
				details.setWorkorder(payload.getWorkorder());

				WorkOrderOpsModel opsdata = new WorkOrderOpsModel();
				opsdata = modelMapper.map(details, WorkOrderOpsModel.class);
				opsrepo.save(opsdata);

			} else {

				if (data.getStartTime() == null) {
					opsrepo.updateEndopsTime(payload.getPlant(), payload.getWorkorder(), data.getOperation(),
							data.getEndTime());
				} else if (data.getEndTime() == null) {
					opsrepo.updateStartopsTime(payload.getPlant(), payload.getWorkorder(), data.getOperation(),
							data.getStartTime());
				} else {
					opsrepo.updateopsTime(payload.getPlant(), payload.getWorkorder(), data.getOperation(),
							data.getEndTime(), data.getStartTime(), data.getPause_duration(), data.getStatus());
				}
			}
		}
		return new ResponseEntity<>("Operaton date have been updated", HttpStatus.OK);
	}

}
