package com.budderfly.sites.web.rest;

import com.budderfly.sites.domain.Site;
import com.budderfly.sites.domain.enumeration.SiteStatus;
import com.budderfly.sites.service.dto.*;
import com.codahale.metrics.annotation.Timed;
import com.budderfly.sites.service.SiteService;
import com.budderfly.sites.web.rest.errors.BadRequestAlertException;
import com.budderfly.sites.web.rest.util.HeaderUtil;
import com.budderfly.sites.web.rest.util.PaginationUtil;
import com.budderfly.sites.service.SiteQueryService;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Site.
 */
@RestController
@RequestMapping("/api")
public class SiteResource {

    private final Logger log = LoggerFactory.getLogger(SiteResource.class);

    private static final String ENTITY_NAME = "site";

    private final SiteService siteService;

    private final SiteQueryService siteQueryService;

    public SiteResource(SiteService siteService, SiteQueryService siteQueryService) {
        this.siteService = siteService;
        this.siteQueryService = siteQueryService;
    }

    /**
     * POST  /sites : Create a new site.
     *
     * @param siteDTO the siteDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new siteDTO, or with status 400 (Bad Request) if the site has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/sites")
    @Timed
    public ResponseEntity<SiteDTO> createSite(@Valid @RequestBody SiteDTO siteDTO) throws URISyntaxException {
        log.debug("REST request to save Site : {}", siteDTO);
        if (siteDTO.getId() != null) {
            throw new BadRequestAlertException("A new site cannot already have an ID", ENTITY_NAME, "idexists");
        }
        SiteDTO result = siteService.save(siteDTO);
        return ResponseEntity.created(new URI("/api/sites/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /sites : Updates an existing site.
     *
     * @param siteDTO the siteDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated siteDTO,
     * or with status 400 (Bad Request) if the siteDTO is not valid,
     * or with status 500 (Internal Server Error) if the siteDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/sites")
    @Timed
    public ResponseEntity<SiteDTO> updateSite(@Valid @RequestBody SiteDTO siteDTO) throws URISyntaxException {
        log.debug("REST request to update Site : {}", siteDTO);
        if (siteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        SiteDTO result = siteService.save(siteDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, siteDTO.getId().toString()))
            .body(result);
    }

    @PreAuthorize("hasPermission(#budderflyId, 'PORTAL')")
    @PutMapping("/sites/portal/{budderflyId}")
    public ResponseEntity<SiteDTO> updatePortalSite(@PathVariable String budderflyId, @RequestBody PortalSiteDTO portalSiteDTO){
        log.debug("REST request to update for portal user site: " + budderflyId);
        SiteDTO site = getSiteByBudderflyId(budderflyId).getBody();
        site.setPaymentType(portalSiteDTO.getPaymentType());

        SiteDTO result = siteService.save(site);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    @GetMapping("/sites/by-email/{email}")
    public ResponseEntity<List<SiteDTO>> getSitesBySiteEmail(@PathVariable String email) {
        log.debug("REST request to get site data from email " + email);
        List<SiteDTO> sites = siteService.findSitesBySiteEmail(email);

        return ResponseEntity.ok().body(sites);
    }

    /**
     * POST  /sites/sync : trigger Sync Sites accounts
     */
    @PostMapping("/sites/sync")
    @Timed
    public void syncSites() {
        log.info("Sync Sytes");
        siteService.syncSites();
    }

    /**
     * POST  /sites/from-site-account : Create a new site from a siteAccountDTO.
     *
     * @param siteAccountDTO the siteDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new siteDTO, or with status 400 (Bad Request) if the site has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/sites/from-site-account")
    @Timed
    public ResponseEntity<SiteDTO> createSiteFromSiteAccount(@RequestBody @Valid SiteAccountDTO siteAccountDTO) throws URISyntaxException {
        log.debug("REST request to save Site : {}", siteAccountDTO);
        SiteDTO site = siteService.createSite(siteAccountDTO);
        return ResponseEntity.ok().body(site);
    }

    @GetMapping("/sites/owned-by-contacts/{email}")
    public ResponseEntity<List<SiteDTO>> getSitesBySiteContacts(@PathVariable String email) {
        List<SiteDTO> sites = siteService.getSiteBasedOnSiteOwnership(email);

        return ResponseEntity.ok(sites);
    }

    /**
     * GET  /sites : get all the sites.
     *
     * @param pageable the pagination information
     * @param criteria the criterias which the requested entities should match
     * @return the ResponseEntity with status 200 (OK) and the list of sites in body
     */
    @GetMapping("/sites")
    @Timed
    public ResponseEntity<List<SiteDTO>> getAllSites(SiteCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Sites by criteria: {}", criteria);
        Page<SiteDTO> page = siteQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/sites");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
    * GET  /sites/count : count all the sites.
    *
    * @param criteria the criterias which the requested entities should match
    * @return the ResponseEntity with status 200 (OK) and the count in body
    */
    @GetMapping("/sites/count")
    @Timed
    public ResponseEntity<Long> countSites(SiteCriteria criteria) {
        log.debug("REST request to count Sites by criteria: {}", criteria);
        return ResponseEntity.ok().body(siteQueryService.countByCriteria(criteria));
    }

    /**
     * GET  /sites/all : get all the Sites.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of sites in body
     */
    @GetMapping("/sites/all")
    @Timed
    public List<SiteDTO> getAllSitesWithOutPage() {
        log.debug("REST request to get all Sites");
        return siteService.findAll();
    }


    /**
     * GET  /sites/all : get all the Sites filtered by site ownership.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of sites in body
     */
    @GetMapping("/sites/all/filtered")
    @Timed
    public List<SiteDTO> getAllSitesWithoutPageFiltered() {
        log.debug("REST request to get all Sites filtered");
        return siteService.findAllFiltered();
    }

    /**
     * GET  /sites/:id : get the "id" site.
     *
     * @param id the id of the siteDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the siteDTO, or with status 404 (Not Found)
     */
    @GetMapping("/sites/{id}")
    @Timed
    public ResponseEntity<SiteDTO> getSite(@PathVariable Long id) {
        log.debug("REST request to get Site : {}", id);
        Optional<SiteDTO> siteDTO = siteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(siteDTO);
    }

    /**
     * DELETE  /sites/:id : delete the "id" site.
     *
     * @param id the id of the siteDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/sites/{id}")
    @Timed
    public ResponseEntity<Void> deleteSite(@PathVariable Long id) {
        log.debug("REST request to delete Site : {}", id);
        siteService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/sites/sites-by-budderfly-id/{budderflyId}")
    @Timed
    public ResponseEntity<SiteDTO> getSiteByBudderflyId(@PathVariable String budderflyId) {
        log.debug("REST request to get sites by budderfly id " + budderflyId);
        SiteDTO siteDTO = siteService.findByBudderflyId(budderflyId);

        return ResponseEntity.ok(siteDTO);
    }

    /**
     * This returns the site object
     * @param budderflyId
     * @return SiteDTO
     * @return 403 if user does not own site (via authenticate.user_site), or not INSTALLER role
     */
    @PreAuthorize("hasPermission(#budderflyId, 'INSTALLER')")
    @GetMapping("/sites/sites-by-budderfly-id-permission/{budderflyId}")
    @Timed
    public ResponseEntity<SiteDTO> getSiteByBudderflyIdPermission(@PathVariable String budderflyId) {
        log.debug("REST request to get site by budderfly id with permission check " + budderflyId);
        SiteDTO siteDTO = siteService.findByBudderflyId(budderflyId);

        return ResponseEntity.ok(siteDTO);
    }

    @GetMapping("/sites/sites-by-status/{status}")
    @Timed
    public ResponseEntity<List<SiteDTO>> getSiteByStatus(@PathVariable SiteStatus status) {
        log.debug("REST request to get sites by status " + status );
        List<SiteDTO> siteDTOS = siteService.findSitesBySiteStatus(status);

        return ResponseEntity.ok(siteDTOS);
    }

    /**
     * SEARCH  /_search/sites?query=:query : search for the site corresponding
     * to the query.
     *
     * @param query the query of the site search
     * @param pageable the pagination information
     * @return the result of the search
     */
    @GetMapping("/_search/sites")
    @Timed
    public ResponseEntity<List<SiteDTO>> searchSites(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of Sites for query {}", query);
        Page<SiteDTO> page = siteService.search(query, pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/sites");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @PostMapping("/sites-by-list")
    public ResponseEntity<List<SiteDTO>> findSitesByList(@RequestBody List<String> budderflyIds) {
        log.debug("REST request to get SiteDTOs from " + budderflyIds);
        List<SiteDTO> sites = siteService.getSitesFromList(budderflyIds);

        return ResponseEntity.ok(sites);
    }

    /**
     * GET  /sites/:id : get the "id" site.
     *
     * @param budderflyId the id of the siteDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the siteDTO, or with status 404 (Not Found)
     */
    @GetMapping("/site-id/{budderflyId}")
    @Timed
    public ResponseEntity<Long> findSiteIdByBudderflyId(@PathVariable String budderflyId) {
        log.debug("REST request to get site ID  for budderfly id {}", budderflyId);
        Long siteId = siteService.findSiteIdByBudderflyId(budderflyId);
        return new ResponseEntity<>(siteId, HttpStatus.OK);
    }

    /*
    Searches through sites based on sites a user owns
    * @param retain boolean value how search should be done
    * True searches against stores the user owns, False searches against all sites except for what the user owns
    * @param siteDTOs list of sites the user owns
    */
    @PostMapping("/_search/owned-by-contacts/{retain}")
    @Timed
    public ResponseEntity<List<SiteDTO>> searchSitesWithOwnership(@RequestBody List<SiteDTO> siteDTOS, @RequestParam String query, @PathVariable Boolean retain, Pageable pageable) {
        log.debug("REST request to search for a page of Sites with ownership for query {}", query);
        List<SiteDTO> siteSearch = new ArrayList<SiteDTO>(siteService.search(query, pageable).getContent());

        if (retain) {
            siteSearch.retainAll(new HashSet<>(siteDTOS));
        } else {
            siteSearch.removeAll(new HashSet<>(siteDTOS));
        }

        Page<SiteDTO> page = new PageImpl<>(siteSearch, pageable, siteSearch.size());
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/owned-by-contacts/" +retain);

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /*
    Returns list of sites a user owns, based on the user_site table
     */
    @GetMapping("/sites/by-authenticate-login/{login}")
    @Timed
    public ResponseEntity<List<SiteDTO>> sitesByAuthenticateLogin(@PathVariable String login, Pageable pageable) {
        log.debug("REST request to get sites by login " + login);
        Page<SiteDTO> page = siteService.getSitesByAuthenticateLogin(login, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/sites/by-authenticate-login/" + login);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /*
    Returns list of sites a user does not own, based on the user_site table
    */
    @GetMapping("/sites/sites-without-authenticate-login/{login}")
    @Timed
    public ResponseEntity<List<SiteDTO>> getSitesWithoutAuthenticateLogin(@PathVariable String login, Pageable pageable) {
        log.debug("REST request to get sites without sites owned by " + login);
        Page<SiteDTO> page = siteService.getSitesWithoutAuthenticateLogin(login, pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/sites/sites-without-authenticate-login/" + login);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * POST  /sites/sync-injobs-data : sync sites information coming from injobs
     *
     * @return the ResponseEntity with status 200 (OK)
     */
    @PostMapping("/sites/sync-injobs-data")
    public ResponseEntity<Void> syncSitesData(@RequestBody List<SiteSyncDTO> listSitesSync){
        log.debug("REST request to sync data received from Injobs");
        this.siteService.syncInjobsData(listSitesSync);
        return ResponseEntity.ok().build();
    }

    /*
     * Mock call to GET work orders and site location
     *
     * @return list of SiteWorkOrdersDTO with status 200 (ok)
     */
    @GetMapping("/site-work-orders")
    public ResponseEntity<List<SiteWorkOrdersDTO>> getWorkOrdersAndSiteDetails() {
        log.debug("REST request to get site details and work orders by ownership");
        List<SiteWorkOrdersDTO> workOrders = this.siteService.getSitesAndWorkOrders();
        return ResponseEntity.ok().body(workOrders);
    }

}
