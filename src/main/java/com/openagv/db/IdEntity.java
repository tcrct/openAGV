package com.openagv.db;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.openagv.db.annotation.Id;
import com.openagv.db.annotation.Param;

import java.util.Date;

/**
 * 所有实体类的基类，子类必须继续该类
 * @author laotang
 * @date 2019-6-13
 */
public class IdEntity implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ENTITY_ID_FIELD = "id";
    public static final String ID_FIELD = "_id";
    public static final String CREATETIME_FIELD = "createtime";
    public static final String CREATEUSERID_FIELD = "createuserid";
    public static final String UPDATETIME_FIELD = "updatetime";
    public static final String UPDATEUSERID_FIELD = "updateuserid";
    public static final String STATUS_FIELD = "status";
    public static final String SOURCE_FIELD = "source";
    public static final String DEPARTIMENTID_FIELD = "departmentId";
    public static final String PROJECTID_FIELD = "projectId";
    public static final String COMPANYID_FIELD = "companyId";
    public static final String STATUS_FIELD_SUCCESS = "审核通过";
    public static final String STATUS_FIELD_DELETE = "已删除";

    @Id
    @JsonProperty(value=ID_FIELD)
    @Param(label = "记录序列号", desc = "该记录的序列号标识,全局唯一")
    private String id;
    /**
     * 创建时间
     */
    @Param(label = "创建时间", desc = "该记录的创建时间")
    private Date createtime;
    /**
     * 创建人ID
     */
    @Param(label = "创建人ID", desc = "该记录的创建人ID")
    private String createuserid;
    /**
     * 更新时间
     */
    @Param(label = "更新时间", desc = "该记录的更新时间")
    private Date updatetime;
    /**
     * 更新人ID
     */
    @Param(label = "更新人ID", desc = "该记录的更新人ID")
    private String updateuserid;
    /**
     * 数据状态(查数据字典)
     */
    @Param(label = "数据状态", desc = "该记录的数据状态")
    private String status;
    /**
     * 数据来源
     */
    @Param(label = "数据来源", desc = "该记录的数据来源")
    private String source;
    /**
     * 公司id标识
     */
    @Param(label = "公司id标识", desc = "该记录的公司id标识")
    private String companyId;
    /**
     * 项目id标识
     */
    @Param(label = "项目id标识", desc = "该记录的项目id标识")
    private String projectId;
    /**
     * 部门id标识
     */
    @Param(label = "部门id标识", desc = "该记录的部门id标识")
    private String departmentId;

    public IdEntity(String id, Date createtime, String createuserid,
                    Date updatetime, String updateuserid, String status, String source, String departmentId, String projectId, String companyId) {
        super();
        this.id = id;
        this.createtime = createtime;
        this.createuserid = createuserid;
        this.updatetime = updatetime;
        this.updateuserid = updateuserid;
        this.status = status;
        this.source = source;
        this.departmentId = departmentId;
        this.projectId = projectId;
        this.companyId = companyId;
    }

    public IdEntity() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setId(int id) {
        this.id = id+"";
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getCreateuserid() {
        return createuserid;
    }

    public void setCreateuserid(String createuserid) {
        this.createuserid = createuserid;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getUpdateuserid() {
        return updateuserid;
    }

    public void setUpdateuserid(String updateuserid) {
        this.updateuserid = updateuserid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    @Override
    public String toString() {
        return "IdEntity{" +
                "id='" + id + '\'' +
                ", createtime=" + createtime +
                ", createuserid='" + createuserid + '\'' +
                ", updatetime=" + updatetime +
                ", updateuserid='" + updateuserid + '\'' +
                ", status='" + status + '\'' +
                ", source='" + source + '\'' +
                ", departmentId='" + departmentId + '\'' +
                ", projectId='" + projectId + '\'' +
                ", companyId='" + companyId + '\'' +
                '}';
    }
}
