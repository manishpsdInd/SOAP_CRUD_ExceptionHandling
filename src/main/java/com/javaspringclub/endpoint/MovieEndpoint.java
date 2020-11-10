package com.javaspringclub.endpoint;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.javaspringclub.entity.MovieEntity;
import com.javaspringclub.exception.ServiceFaultException;
import com.javaspringclub.gs_ws.AddMovieRequest;
import com.javaspringclub.gs_ws.AddMovieResponse;
import com.javaspringclub.gs_ws.DeleteMovieRequest;
import com.javaspringclub.gs_ws.DeleteMovieResponse;
import com.javaspringclub.gs_ws.GetAllMoviesRequest;
import com.javaspringclub.gs_ws.GetAllMoviesResponse;
import com.javaspringclub.gs_ws.GetMovieByIdRequest;
import com.javaspringclub.gs_ws.GetMovieByIdResponse;
import com.javaspringclub.gs_ws.MovieType;
import com.javaspringclub.gs_ws.ServiceStatus;
import com.javaspringclub.gs_ws.UpdateMovieRequest;
import com.javaspringclub.gs_ws.UpdateMovieResponse;
import com.javaspringclub.service.MovieEntityService;

@Endpoint
public class MovieEndpoint {

	public static final String NAMESPACE_URI = "http://www.javaspringclub.com/movies-ws";

	private MovieEntityService service;

	public MovieEndpoint() {

	}

	@Autowired
	public MovieEndpoint(MovieEntityService service) {
		this.service = service;
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "getMovieByIdRequest")
	@ResponsePayload
	public GetMovieByIdResponse getMovieById(@RequestPayload GetMovieByIdRequest request) {
		GetMovieByIdResponse response = new GetMovieByIdResponse();
		MovieEntity movieEntity = service.getEntityById(request.getMovieId());
		MovieType movieType = new MovieType();
		BeanUtils.copyProperties(movieEntity, movieType);
		response.setMovieType(movieType);
		return response;

	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "getAllMoviesRequest")
	@ResponsePayload
	public GetAllMoviesResponse getAllMovies(@RequestPayload GetAllMoviesRequest request) {
		GetAllMoviesResponse response = new GetAllMoviesResponse();
		List<MovieType> movieTypeList = new ArrayList<MovieType>();
		List<MovieEntity> movieEntityList = service.getAllEntities();
		for (MovieEntity entity : movieEntityList) {
			MovieType movieType = new MovieType();
			BeanUtils.copyProperties(entity, movieType);
			movieTypeList.add(movieType);
		}
		response.getMovieType().addAll(movieTypeList);

		return response;

	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "addMovieRequest")
	@ResponsePayload
	public AddMovieResponse addMovie(@RequestPayload AddMovieRequest request) {
		AddMovieResponse response = new AddMovieResponse();
		MovieType newMovieType = new MovieType();
		ServiceStatus serviceStatus = new ServiceStatus();

		MovieEntity newMovieEntity = new MovieEntity(request.getTitle(), request.getCategory());
		MovieEntity savedMovieEntity = service.addEntity(newMovieEntity);

		if (savedMovieEntity == null) {
			serviceStatus.setStatusCode("CONFLICT");
			serviceStatus.setMessage("Exception while adding Entity");
		} else {

			BeanUtils.copyProperties(savedMovieEntity, newMovieType);
			serviceStatus.setStatusCode("SUCCESS");
			serviceStatus.setMessage("Content Added Successfully");
		}

		response.setMovieType(newMovieType);
		response.setServiceStatus(serviceStatus);
		return response;

	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "updateMovieRequest")
	@ResponsePayload
	public UpdateMovieResponse updateMovie(@RequestPayload UpdateMovieRequest request) {
		UpdateMovieResponse response = new UpdateMovieResponse();
		ServiceStatus serviceStatus = new ServiceStatus();
		// 1. Find if movie available
		MovieEntity movieFromDB = service.getEntityByTitle(request.getTitle());
		
		if(movieFromDB == null) {
			serviceStatus.setStatusCode("NOT FOUND");
			serviceStatus.setMessage("Movie = " + request.getTitle() + " not found");
		}else {
			
			// 2. Get updated movie information from the request
			movieFromDB.setTitle(request.getTitle());
			movieFromDB.setCategory(request.getCategory());
			// 3. update the movie in database
			
			boolean flag = service.updateEntity(movieFromDB);
			
			if(flag == false) {
				serviceStatus.setStatusCode("CONFLICT");
				serviceStatus.setMessage("Exception while updating Entity=" + request.getTitle());;
			}else {
				serviceStatus.setStatusCode("SUCCESS");
				serviceStatus.setMessage("Content updated Successfully");
			}
			
			
		}
		
		response.setServiceStatus(serviceStatus);
		return response;
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "deleteMovieRequest")
	@ResponsePayload
	public DeleteMovieResponse deleteMovie(@RequestPayload DeleteMovieRequest request) throws ServiceFaultException {
		DeleteMovieResponse response = new DeleteMovieResponse();
		ServiceStatus serviceStatus = new ServiceStatus();

		boolean flag = service.deleteEntityById(request.getMovieId());

		if (flag == false) {
			serviceStatus.setStatusCode("FAIL");
			serviceStatus.setMessage("Exception while deleting Entity id=" + request.getMovieId());
		} else {
			serviceStatus.setStatusCode("SUCCESS");
			serviceStatus.setMessage("Content Deleted Successfully");
		}

		response.setServiceStatus(serviceStatus);
		return response;
	}

}
