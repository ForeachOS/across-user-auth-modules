package com.foreach.across.modules.entity.controllers;

import com.foreach.across.modules.adminweb.AdminWeb;
import com.foreach.across.modules.adminweb.annotations.AdminWebController;
import com.foreach.across.modules.adminweb.menu.AdminMenu;
import com.foreach.across.modules.adminweb.menu.EntityAdminMenu;
import com.foreach.across.modules.entity.business.EntityForm;
import com.foreach.across.modules.entity.business.EntityWrapper;
import com.foreach.across.modules.entity.config.EntityConfiguration;
import com.foreach.across.modules.entity.services.EntityFormFactory;
import com.foreach.across.modules.entity.services.EntityRegistry;
import com.foreach.across.modules.hibernate.business.IdBasedEntity;
import com.foreach.across.modules.web.menu.MenuFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@AdminWebController
@RequestMapping("/entities/{entityConfig}")
public class EntitySaveController
{
	@Autowired
	private AdminWeb adminWeb;

	@Autowired
	private EntityRegistry entityRegistry;

	@Autowired
	private EntityFormFactory formFactory;

	@Autowired
	private MenuFactory menuFactory;

	@Autowired
	private Validator entityValidatorFactory;

	@InitBinder
	protected void initBinder( WebDataBinder binder ) {
		binder.setValidator( entityValidatorFactory );
	}

	@ModelAttribute("entity")
	public Object entity( @PathVariable("entityConfig") String entityType,
	                      @RequestParam(value = "id", required = false) Long entityId,
	                      ModelMap model
	) throws Exception {
		EntityConfiguration entityConfiguration = entityRegistry.getEntityByPath( entityType );

		Object entity = entityConfiguration.getEntityClass().newInstance();

		if ( entityId != null && entityId != 0 ) {
			Object original = entityConfiguration.getRepository().getById( entityId );
			model.addAttribute( "original", entityConfiguration.wrap( original ) );
			BeanUtils.copyProperties( original, entity );
		}

		return entity;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/*", method = RequestMethod.POST)
	public String saveEntity( @PathVariable("entityConfig") String entityType,
	                          @ModelAttribute("entity") @Valid Object entity,
	                          BindingResult bindingResult,
	                          ModelMap model,
	                          AdminMenu adminMenu,
	                          RedirectAttributes re ) {
		EntityConfiguration entityConfiguration = entityRegistry.getEntityByPath( entityType );

		if ( !bindingResult.hasErrors() ) {
			if ( isNewEntity( entity ) ) {
				entityConfiguration.getRepository().create( entity );
			}
			else {
				entityConfiguration.getRepository().update( entity );
			}

			re.addAttribute( "entityId", ( (IdBasedEntity) entity ).getId() );

			return adminWeb.redirect( "/entities/" + entityConfiguration.getPath() + "/{entityId}" );
		}
		else {
			EntityWrapper originalEntity = (EntityWrapper) model.get( "original" );

			if ( originalEntity != null ) {
				adminMenu.getLowestSelectedItem()
				         .addItem( "/selectedEntity", originalEntity.getEntityLabel() )
				         .setSelected( true );
				model.addAttribute( "entityMenu",
				                    menuFactory.buildMenu(
						                    new EntityAdminMenu(
								                    entityConfiguration.getEntityClass(),
								                    originalEntity.getEntity()
						                    )
				                    )
				);
			}
			else {
				model.addAttribute( "entityMenu",
				                    menuFactory.buildMenu(
						                    new EntityAdminMenu( entityConfiguration.getEntityClass() )
				                    )
				);
			}

			EntityForm entityForm = formFactory.create( entityConfiguration );
			entityForm.setEntity( entity );

			model.addAttribute( "entityForm", entityForm );
			model.addAttribute( "existing", originalEntity != null );
			model.addAttribute( "entityConfig", entityConfiguration );
			//model.addAttribute( "entity", entityConfiguration.wrap( entity ) );
		}

		return "th/entity/edit";
	}

	private boolean isNewEntity( Object entity ) {
		return ( (IdBasedEntity) entity ).getId() == 0;
	}
}
