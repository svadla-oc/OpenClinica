/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.submit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * @author thickerson
 * 
 * 
 */
public class ItemDAO extends AuditableEntityDAO {
    // private DAODigester digester;

    public ItemDAO(DataSource ds) {
        super(ds);
    }

    public ItemDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public ItemDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_ITEM;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);
        this.setTypeExpected(5, TypeNames.BOOL);// phi status
        this.setTypeExpected(6, TypeNames.INT);// data type id
        this.setTypeExpected(7, TypeNames.INT);// reference type id
        this.setTypeExpected(8, TypeNames.INT);// status id
        this.setTypeExpected(9, TypeNames.INT);// owner id
        this.setTypeExpected(10, TypeNames.DATE);// created
        this.setTypeExpected(11, TypeNames.DATE);// updated
        this.setTypeExpected(12, TypeNames.INT);// update id
        this.setTypeExpected(13, TypeNames.STRING);// oc_oid
    }

    public EntityBean update(EntityBean eb) {
        ItemBean ib = (ItemBean) eb;
        HashMap variables = new HashMap();
        variables.put(new Integer(1), ib.getName());
        variables.put(new Integer(2), ib.getDescription());
        variables.put(new Integer(3), ib.getUnits());
        variables.put(new Integer(4), new Boolean(ib.isPhiStatus()));
        variables.put(new Integer(5), new Integer(ib.getItemDataTypeId()));
        variables.put(new Integer(6), new Integer(ib.getItemReferenceTypeId()));
        variables.put(new Integer(7), new Integer(ib.getStatus().getId()));
        variables.put(new Integer(8), new Integer(ib.getUpdaterId()));
        variables.put(new Integer(9), new Integer(ib.getId()));
        this.execute(digester.getQuery("update"), variables);
        return eb;
    }

    public EntityBean create(EntityBean eb) {
        ItemBean ib = (ItemBean) eb;
        // per the create sql statement
        HashMap variables = new HashMap();
        variables.put(new Integer(1), ib.getName());
        variables.put(new Integer(2), ib.getDescription());
        variables.put(new Integer(3), ib.getUnits());
        variables.put(new Integer(4), new Boolean(ib.isPhiStatus()));
        variables.put(new Integer(5), new Integer(ib.getItemDataTypeId()));
        variables.put(new Integer(6), new Integer(ib.getItemReferenceTypeId()));
        variables.put(new Integer(7), new Integer(ib.getStatus().getId()));
        variables.put(new Integer(8), new Integer(ib.getOwnerId()));
        // date_created=now() in Postgres
        this.execute(digester.getQuery("create"), variables);
        // set the id here????
        return eb;
    }

    private String getOid(ItemBean itemBean, String crfName, String itemLabel) {

        String oid;
        try {
            oid = itemBean.getOid() != null ? itemBean.getOid() : itemBean.getOidGenerator().generateOid(crfName, itemLabel);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    public Integer getCountofActiveItems() {
        setTypesExpected();

        String sql = digester.getQuery("getCountofItems");

        ArrayList rows = this.select(sql);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public String getValidOid(ItemBean itemBean, String crfName, String itemLabel, ArrayList<String> oidList) {

        String oid = getOid(itemBean, crfName, itemLabel);
        logger.info(oid);
        String oidPreRandomization = oid;
        while (findByOid(oid).size() > 0 || oidList.contains(oid)) {
            oid = itemBean.getOidGenerator().randomizeOid(oidPreRandomization);
        }
        return oid;

    }

    public Object getEntityFromHashMap(HashMap hm) {
        ItemBean eb = new ItemBean();
        // below inserted to find out a class cast exception, tbh
        Date dateCreated = (Date) hm.get("date_created");
        Date dateUpdated = (Date) hm.get("date_updated");
        Integer statusId = (Integer) hm.get("status_id");
        Integer ownerId = (Integer) hm.get("owner_id");
        Integer updateId = (Integer) hm.get("update_id");

        eb.setCreatedDate(dateCreated);
        eb.setUpdatedDate(dateUpdated);
        eb.setStatus(Status.get(statusId.intValue()));
        eb.setOwnerId(ownerId.intValue());
        eb.setUpdaterId(updateId.intValue());
        // something to trip over
        // something else to trip over
        // eb = (ItemBean)this.getEntityAuditInformation(hm);
        eb.setName((String) hm.get("name"));
        eb.setId(((Integer) hm.get("item_id")).intValue());
        eb.setDescription((String) hm.get("description"));
        eb.setUnits((String) hm.get("units"));
        eb.setPhiStatus(((Boolean) hm.get("phi_status")).booleanValue());
        eb.setItemDataTypeId(((Integer) hm.get("item_data_type_id")).intValue());
        eb.setItemReferenceTypeId(((Integer) hm.get("item_reference_type_id")).intValue());
        // logger.info("item name|date type id" + eb.getName() + "|" +
        // eb.getItemDataTypeId());
        eb.setDataType(ItemDataType.get(eb.getItemDataTypeId()));
        eb.setOid((String) hm.get("oc_oid"));
        // the rest should be all set
        return eb;
    }

    public List<ItemBean> findByOid(String oid) {
        this.setTypesExpected();
        HashMap<Integer, String> variables = new HashMap<Integer, String>();
        variables.put(1, oid);
        List listofMaps = this.select(digester.getQuery("findItemByOid"), variables);

        List<ItemBean> beanList = new ArrayList<ItemBean>();
        ItemBean bean;
        for (Object map : listofMaps) {
            bean = (ItemBean) this.getEntityFromHashMap((HashMap) map);
            beanList.add(bean);
        }
        return beanList;
    }

    public Collection findAll() {
        this.setTypesExpected();
        ArrayList alist = this.select(digester.getQuery("findAll"));
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            ItemBean eb = (ItemBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public ArrayList findAllParentsBySectionId(int sectionId) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(sectionId));

        return this.executeFindAllQuery("findAllParentsBySectionId", variables);
    }

    public ArrayList findAllBySectionId(int sectionId) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(sectionId));

        return this.executeFindAllQuery("findAllBySectionId", variables);
    }

    public ArrayList findAllUngroupedParentsBySectionId(int sectionId, int crfVersionId) {
        HashMap variables = new HashMap();
        variables.put(1, sectionId);
        variables.put(2, crfVersionId);

        return this.executeFindAllQuery("findAllUngroupedParentsBySectionId", variables);
    }

    public ArrayList findAllItemsByVersionId(int versionId) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(versionId));

        return this.executeFindAllQuery("findAllItemsByVersionId", variables);
    }

    public ArrayList findAllVersionsByItemId(int itemId) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(itemId));
        String sql = digester.getQuery("findAllVersionsByItemId");
        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            Integer versionId = (Integer) ((HashMap) it.next()).get("crf_version_id");
            al.add(versionId);
        }
        return al;

    }

    public List<ItemBean> findAllItemsByGroupId(int id, int crfVersionId) {
        this.setTypesExpected();
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(1, id);
        variables.put(2, crfVersionId);
        String sql = digester.getQuery("findAllItemsByGroupId");
        List listofMaps = this.select(sql, variables);
        List<ItemBean> beanList = new ArrayList<ItemBean>();
        ItemBean bean;
        for (Object map : listofMaps) {
            bean = (ItemBean) this.getEntityFromHashMap((HashMap) map);
            beanList.add(bean);
        }
        return beanList;
    }

    public ItemBean findItemByGroupIdandItemOid(int id, String itemOid) {
        ItemBean bean;
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(1, id);
        variables.put(2, itemOid);
        String sql = digester.getQuery("findItemByGroupIdandItemOid");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            bean = (ItemBean) this.getEntityFromHashMap((HashMap) it.next());
            return bean;
        } else {
            return null;
        }
    }

    public ArrayList findAllActiveByCRF(CRFBean crf) {
        HashMap variables = new HashMap();
        this.setTypesExpected();
        this.setTypeExpected(14, TypeNames.INT);// crf_version_id
        this.setTypeExpected(15, TypeNames.STRING);// version name
        variables.put(new Integer(1), new Integer(crf.getId()));
        String sql = digester.getQuery("findAllActiveByCRF");
        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            ItemBean eb = (ItemBean) this.getEntityFromHashMap(hm);
            Integer versionId = (Integer) hm.get("crf_version_id");
            String versionName = (String) hm.get("cvname");
            ItemFormMetadataBean imf = new ItemFormMetadataBean();
            imf.setCrfVersionName(versionName);
            // logger.info("versionName" + imf.getCrfVersionName());
            imf.setCrfVersionId(versionId.intValue());
            eb.setItemMeta(imf);
            al.add(eb);
        }
        return al;

    }

    public EntityBean findByPK(int ID) {
        ItemBean eb = new ItemBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (ItemBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return eb;
    }

    public EntityBean findByName(String name) {
        ItemBean eb = new ItemBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), name);

        String sql = digester.getQuery("findByName");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (ItemBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return eb;
    }

    public EntityBean findByNameAndCRFId(String name, int crfId) {
        ItemBean eb = new ItemBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), name);
        variables.put(new Integer(2), new Integer(crfId));

        String sql = digester.getQuery("findByNameAndCRFId");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (ItemBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return eb;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        ArrayList al = new ArrayList();

        return al;
    }

    /**
     * Finds the children of an item in a given CRF Version, sorted by
     * columnNumber in ascending order.
     * 
     * @param parentId
     *            The id of the children's parent.
     * @param crfVersionId
     *            The id of the event CRF in which the children belong to this
     *            parent.
     * @return An array of ItemBeans, where each ItemBean represents a child of
     *         the parent and the array is sorted by columnNumber in ascending
     *         order.
     */
    public ArrayList findAllByParentIdAndCRFVersionId(int parentId, int crfVersionId) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(parentId));
        variables.put(new Integer(2), new Integer(crfVersionId));

        return this.executeFindAllQuery("findAllByParentIdAndCRFVersionId", variables);
    }

    public int findAllRequiredByCRFVersionId(int crfVersionId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        int answer = 0;

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(crfVersionId));
        String sql = digester.getQuery("findAllRequiredByCRFVersionId");

        ArrayList rows = select(sql, variables);

        if (rows.size() > 0) {
            HashMap row = (HashMap) rows.get(0);
            answer = ((Integer) row.get("number")).intValue();
        }

        return answer;
    }

    public ArrayList findAllRequiredBySectionId(int sectionId) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(sectionId));

        return this.executeFindAllQuery("findAllRequiredBySectionId", variables);
    }
    
    public Map<String,Integer> mapAllItemNameAndItemIdInSection(Integer sectionId) {
        Map<String,Integer> nameIdMap = new HashMap<String,Integer>();
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); //item_id
        this.setTypeExpected(2, TypeNames.STRING); //(item)name
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(sectionId));
        String sql = digester.getQuery("findIdAndNamesInSection");
        ArrayList rows = select(sql, variables);
        Iterator it = rows.iterator();
        while (it.hasNext()) {
            HashMap row = (HashMap) it.next();
            Integer id = (Integer) row.get("item_id");
            String name = (String) row.get("name");
            nameIdMap.put(name, id);
        }
        return nameIdMap;
    }
    
    public Map<String,String> mapAllChildAndParentNameInSection(Integer sectionId) {
        Map<String,String> nameMap = new HashMap<String,String>();
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.STRING);//(item)name
        this.setTypeExpected(2, TypeNames.INT);//item_id
        this.setTypeExpected(3, TypeNames.STRING);//parent_name
        this.setTypeExpected(4, TypeNames.INT);//parent_id
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(sectionId));
        variables.put(new Integer(2), new Integer(sectionId));
        String sql = digester.getQuery("findChildAndParentNamesInSection");
        ArrayList rows = select(sql, variables);
        Iterator it = rows.iterator();
        while (it.hasNext()) {
            HashMap row = (HashMap) it.next();
            String cn = (String) row.get("name");
            Integer cid = (Integer) row.get("item_id");
            String pn = (String) row.get("parent_name");
            Integer pid = (Integer) row.get("parent_id");
            nameMap.put(cn, pn);
        }
        return nameMap;
    }
}
