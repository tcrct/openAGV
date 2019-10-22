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
    private Date createTime;
    /**
     * 创建人ID
     */
    @Param(label = "创建人ID", desc = "该记录的创建人ID")
    private String createUserId;
    /**
     * 更新时间
     */
    @Param(label = "更新时间", desc = "该记录的更新时间")
    private Date updateTime;
    /**
     * 更新人ID
     */
    @Param(label = "更新人ID", desc = "该记录的更新人ID")
    private String updateUserId;
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

    public IdEntity(String id, Date createTime, String createUserId, Date updateTime, String updateUserId, String status, String source) {
        super();
        this.id = id;
        this.createTime = createTime;
        this.createUserId = createUserId;
        this.updateTime = updateTime;
        this.updateUserId = updateUserId;
        this.status = status;
        this.source = source;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
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

    @Override
    public String toString() {
        return "IdEntity{" +
                "id='" + id + '\'' +
                ", createtime=" + createTime +
                ", createuserid='" + createUserId + '\'' +
                ", updatetime=" + updateTime +
                ", updateuserid='" + updateUserId + '\'' +
                ", status='" + status + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}
