package com.tsdata.model.factor.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.tsdata.model.factor.common.CommonUtil;
import com.tsdata.model.factor.common.HttpClientUtils;
import com.tsdata.model.factor.common.SecretUtils;
import com.tsdata.model.factor.service.GetInterfaceInfoService;

import net.sf.json.JSONObject;

/**
 * 阿福共享版接口
 * 
 * @author 31716
 *
 */
@Service("AFuShareHandler")
public class GetAFuShareResultServiceImpl implements GetInterfaceInfoService {
	public static Logger logger = LoggerFactory.getLogger(GetAFuShareResultServiceImpl.class);
	@Value("${AFu.url}")
	private String url;

	@Value("${Afu.userName}")
	private String userName;

	@Value("${Afu.sign}")
	private String sign;

	@Value("${Afu.queryReason}")
	private String queryReason;

	@Override
	public JSONObject getResult(JSONObject param) {
		logger.info(">>>>调用阿福共享版接口>>>>>");
		JSONObject result = new JSONObject();

		if (!param.containsKey("name") || !param.containsKey("id_no") || !StringUtils.hasText(param.getString("name"))
				|| !StringUtils.hasText(param.getString("id_no"))) {

			result.put("success", "false");
			result.put("data", "");
			return result;
		}
		String name = param.getString("name");
		String idNo = param.getString("id_no");

		Map loanRecord = new HashMap();
		loanRecord.put("approvalStatus", "ACCEPT");// 审批结果码
		loanRecord.put("idNo", "370633197005042513");// 被查询借款人身份证号
		loanRecord.put("loanAmount", 20);// 借款金额，通过的，取合同金额;未通过或审核中的， 取申请金额
		loanRecord.put("loanDate", "202003");// 借款时间，通过的，取合同时间;未通过或审核中的， 取申请时间。作为数据提供方，平台可识别的时间格式有 2 种:YYYYMM或者YYYYMMDD
		loanRecord.put("loanStatus", "OVERDUE");// 还款状态码，指一笔借款合同当前的状态;若历史出现过逾期，当前还款正常，则还款状态取“正常”
		loanRecord.put("loanType", "CREDIT");// 借款类型码，指一笔借款所属的类型
		loanRecord.put("name", "测试");// 被查询借款人姓名
		loanRecord.put("overdueAmount", 20);// 逾期金额，指一笔借款中，达到还款期限，尚未偿还 的总金额
		loanRecord.put("overdueM3", 1);// 历史逾期 M3+次数(不含 M3，包括 M6 及以上)
		loanRecord.put("overdueM6", 1);// 历史逾期 M6+次数(不含 M6)
		loanRecord.put("overdueStatus", "M3+");// 逾期情况，指一笔借款当前逾期的程度
		loanRecord.put("overdueTotal", 5);// 历史逾期总次数
		loanRecord.put("periods", 36);// 期数, , 通过的，取合同期数;未通过或审核中的，取 申请期数，范围1~120
		Map riskResult = new HashMap();// 风险项记录
		riskResult.put("riskDetail", "命中外部黑名单");// 风险明细，合作机构提供的借款人的风险类别
		riskResult.put("riskItemType", "ID_NO");// 命中项码，如证件号码(当前命中项仅包括证件号码)
		riskResult.put("riskItemValue", "370633197005042513");// 命中内容，身份证号的具体值
		riskResult.put("riskTime", "2017");// 风险最近时间，指风险记录最近一次发现的时间 平台可识别的时间格式有 3 种:YYYY YYYYMM YYYYMMDD
										
		List<Map> loanRecords = new ArrayList<>();
		loanRecords.add(loanRecord);
		List<Map> riskResults = new ArrayList<>();
		riskResults.add(riskResult);

		JSONObject resultJson = new JSONObject();//
		JSONObject dataJson = new JSONObject(); // 响应结果
		dataJson.put("loanRecords", loanRecords);
		dataJson.put("riskResults", riskResults);
		try {
			String strData1 = SecretUtils.encode(dataJson.toString(), sign);
			String strData2 = URLEncoder.encode(strData1, "UTF-8");
			result.put("success", "true");
			result.put("data", strData2);
			return result;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result.put("success", "false");
			result.put("data", "");
			return result;
		}

	}

}
