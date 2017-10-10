package Utils;

import java.io.Serializable;

/**
 * <b>类描述：</b>统一封装业务方法调用的返回结�?<br/>
 * <b>类名称：</b>MethodResult<br/>
 * <b>创建人：</b>余敏</a><br/>
 * <b>关键修改�?</b><br/>
 * <b>修改时间�?</b><br/>
 * <b>修改人：</b><br/>
 * 
 */
public class MethodResult implements Serializable {
  private static final long serialVersionUID = -3518888906999804609L;
  
  public MethodResult(int result, String resultMsg) {
    super();
    this.result = result;
    this.resultMsg = resultMsg;
  }
  
  public MethodResult(int result, String resultMsg,Object obj) {
    super();
    this.result = result;
    this.resultMsg = resultMsg;
    this.object=obj;
  }

  public MethodResult() {
	  this(0,"");
  }

  /**
   * SUCCESS,FAILURE:结果标识�?0代表失败�?1代表成功。add by leeo 2011-05-07
   */
  public static final int SUCCESS=1;
  public static final int FAILURE=0;
  
  /**
   * result:结果标识�?1代表成功，其它标识码由服务调用�?�和服务提供者约�?
   */
  private int result;
  
  /**
   * resultMsg:其它返回信息，对方法的调用结果进行有意义的描�?
   */
  private String resultMsg;
  
  
  /**
   * object:
   */
  private Object object;

  public void setResult(int result) {
    this.result = result;
  }

  /**
   * 
   * @return 结果标识 1为成�? 其它由服务调用�?�和服务提供者约�? 服务提供者必�?在服务方法上说明�?有可能的返回�?
   */
  public int getResult() {
    return result;
  }

  public void setObject(Object object) {
    this.object = object;
  }

  /**
   * @return 其它返回信息,由服务调用�?�和服务提供者约�? 服务提供者必�?在服务方法上说明该对�?
   */
  public Object getObject() {
    return object;
  }

  public void setResultMsg(String resultMsg) {
    this.resultMsg = resultMsg;
  }

  /**
   * 
   * @return 结果提示信息
   */
  public String getResultMsg() {
    return resultMsg;
  }

}
