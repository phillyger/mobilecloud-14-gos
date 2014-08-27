/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video.controller;

import java.security.Principal;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
public class VideoSvc {

	
	// Auto wire in the repo dependency
	@Autowired
	private VideoRepository videos;
	
	// get video list
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH,method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList(){
		return Lists.newArrayList(videos.findAll());
	}
	
	// get video by id
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/{id}", method=RequestMethod.GET)
	public @ResponseBody Video getVideoById(@PathVariable("id") long id, HttpServletResponse response) {

		Video v = findVideoById(id, response);	
		return v;
	}
	
	// add video
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		Video savedVideo = videos.save(v);
		return savedVideo;
	}
	
	// like video
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/{id}/like",method=RequestMethod.POST)
	public @ResponseBody void likeVideo(@PathVariable("id") long id, HttpServletResponse response, Principal user) {

		Video v = findVideoById(id, response);
		if (v == null) {
			// Video not found
			return;
		}
		
		// Check if the video has been liked already
		if (v.like(user.getName()) == false)
		{
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		videos.save(v);
		response.setStatus(HttpServletResponse.SC_OK);
		
	}
	
	// unlike video
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/{id}/unlike", method=RequestMethod.POST)
	public @ResponseBody void unlikeVideo(@PathVariable("id") long id, HttpServletResponse response, Principal user) {
		
		Video v = findVideoById(id, response);
		if (v == null) {
			// Video not found
			return;
		}
		
		// Check if the video has been un-liked already
		if (v.unlike(user.getName()) == false)
		{
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		videos.save(v);
		response.setStatus(HttpServletResponse.SC_OK);
		
	}
	
	// users who liked the video
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method=RequestMethod.GET)
	public @ResponseBody Collection<String> getUsersWhoLikedVideo(@PathVariable("id") long id, HttpServletResponse response ){
		
		Video v = videos.findOne(id);
		
		if (v == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		
		return v.getLikedBy();
	}
	
	// find by title
	@RequestMapping(value=VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByTitle(@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title) {
		
		return videos.findByName(title);
	}
	
	
	// find by duration
	@RequestMapping(value=VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(@RequestParam(VideoSvcApi.DURATION_PARAMETER) long maxDuration) {
		
		return videos.findByDurationLessThan(maxDuration);
	}

	
	private Video findVideoById(long id, HttpServletResponse response)
	{
		Video v = videos.findOne(id);
		if (v == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		
		return v;
	}
	
}
