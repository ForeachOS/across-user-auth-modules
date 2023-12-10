/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.user.controllers;

import com.foreach.across.modules.adminweb.annotations.AdminWebController;
import com.foreach.across.modules.user.business.QGroup;
import com.foreach.across.modules.user.repositories.GroupRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Arne Vandamme
 */
@AdminWebController
@RequestMapping("/autosuggest")
@ResponseBody
public class AutoSuggestController
{
	@Autowired
	private GroupRepository groupRepository;

	@RequestMapping("/groups")
	@SuppressWarnings("unused")
	public List<AutosuggestItemViewHelper> suggestGroups( @RequestParam(value = "query") String query ) {
		QGroup group = QGroup.group;

		return StreamSupport
				.stream( groupRepository.findAll( group.name.containsIgnoreCase( query ) ).spliterator(),
				         false )
				.map( item -> new AutosuggestItemViewHelper( item.getId(), item.getName() ) )
				.collect( Collectors.toList() );
	}

	@Getter
	@Setter
	@RequiredArgsConstructor
	public static class AutosuggestItemViewHelper
	{
		private final Long id;
		private final String description;
	}
}
