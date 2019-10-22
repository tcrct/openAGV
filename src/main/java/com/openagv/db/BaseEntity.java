package com.openagv.db;
import com.openagv.db.annotation.Param;

import java.util.Date;

/**
 * 所有实体类基类，增加公司，项目，部门ID标识
 *
 * @author laotang
 * @date 2019-6-13
 */
public class BaseEntity extends IdEntity {

    public static final String DEPARTIMENTID_FIELD = "departmentId";
    public static final String PROJECTID_FIELD = "projectId";
    public static final String COMPANYID_FIELD = "companyId";

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

    public BaseEntity() {
        super();
    }

    public BaseEntity(String id, Date createTime, String createUserId, Date updateTime, String updateUserId, String status, String source, String departmentId, String projectId, String companyId) {
        super(id, createTime, createUserId, updateTime, updateUserId, status, source);
        this.companyId = companyId;
        this.projectId = projectId;
        this.departmentId = departmentId;
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

}
