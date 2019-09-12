package com.gugy.activiti6.controller.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gugy.activiti6.common.BaseResponse;
import com.gugy.activiti6.enums.StatusCode;
import com.gugy.activiti6.model.PageData;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author guguangyu
 * @title: MyModelController
 * @projectName activiti6
 * @description: TODO
 * @date 2019/9/1110:27
 */
@RestController
@RequestMapping("/act")
public class MyModelController {

    @Autowired
    RepositoryService repositoryService;
    @Autowired
    ObjectMapper objectMapper;


    /**
     * @description: TODO layui分页获取
     * @param
     * @return
     * @throws
     * @author guguangyu
     * @date 2019/9/11 10:07
     */
    @PostMapping("/pageList")
    public BaseResponse getList(@RequestBody PageData pageData){
        BaseResponse response;
        int pageNum = Integer.valueOf(pageData.get("page") + "");   //起始页
        int pageSize = Integer.valueOf(pageData.get("limit") + ""); //每页条数
        List<Model> list = repositoryService.createModelQuery().listPage(pageSize * (pageNum - 1)
                , pageSize);
        long count = repositoryService.createModelQuery().count();
        response = new BaseResponse(StatusCode.Success.getCode(),StatusCode.Success.getMsg(),count,list);
        return  response;
    }


    /**
     * @description: TODO  新建流程
     * @param
     * @return
     * @throws
     * @author guguangyu
     * @date 2019/9/11 11:05
     */
    @PostMapping("/createModel")
    public BaseResponse createModel(@RequestBody PageData pageData) throws Exception{
        BaseResponse response;
        //初始化一个空模型
        Model model = repositoryService.newModel();

        //设置一些默认信息
        String name = "新建流程";
        String description = "";
        int revision = 1;
        String key = "process";

        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);

        model.setName(name);
        model.setKey(key);
        model.setMetaInfo(modelNode.toString());

        repositoryService.saveModel(model);
        String id = model.getId();
        //完善ModelEditorSource
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace",
                "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.put("stencilset", stencilSetNode);
        repositoryService.addModelEditorSource(id,editorNode.toString().getBytes("utf-8"));
        response = new BaseResponse(StatusCode.Success.getCode(),StatusCode.Success.getMsg(),id);
        return  response;
    }


    /**
     * @description: TODO 删除流程
     * @param
     * @return
     * @throws
     * @author guguangyu
     * @date 2019/9/11 13:27
     */
    @DeleteMapping("/{id}")
    public BaseResponse delete(@PathVariable("id") String id){
        BaseResponse response;
        repositoryService.deleteModel(id);
        response = new BaseResponse(StatusCode.Success);
        return response;
    }


    /**
     * @description: TODO 获取model
     * @param
     * @return
     * @throws
     * @author guguangyu
     * @date 2019/9/11 13:59
     */
    @GetMapping("/{id}")
    public BaseResponse getModel(@PathVariable String id){
        BaseResponse response;
        Model model = repositoryService.createModelQuery().modelId(id).singleResult();
        response = new BaseResponse(StatusCode.Success.getCode(),StatusCode.Success.getMsg(),model);
        return response;
    }

    /**
     * @description: TODO  发布流程
     * @param
     * @return
     * @throws
     * @author guguangyu
     * @date 2019/9/11 13:31
     */
    @PostMapping("/deployment/{id}")
    public  BaseResponse deployment(@PathVariable String id) throws Exception{
        BaseResponse response;
        //获取模型
        Model modelData = repositoryService.getModel(id);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            response = new BaseResponse(StatusCode.Fail.getCode(),StatusCode.Fail.getMsg(),"模型数据为空，请先设计流程并成功保存，再进行发布。");
            return response;
        }

        JsonNode modelNode = new ObjectMapper().readTree(bytes);

        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        if(model.getProcesses().size()==0){
            response = new BaseResponse(StatusCode.Fail.getCode(),StatusCode.Fail.getMsg(),"数据模型不符要求，请至少设计一条主线流程。");
            return response;
        }
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

        //发布流程
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, new String(bpmnBytes, "UTF-8"))
                .deploy();
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);
        response = new BaseResponse(StatusCode.Success);
        return response;
    }


    /**
     * @description: TODO 获取已发布的流程分页
     * @param
     * @return
     * @throws
     * @author guguangyu
     * @date 2019/9/11 15:44
     */
    @PostMapping("/pageListDeployment")
    public BaseResponse pageListDeployment(@RequestBody PageData pageData){
        BaseResponse response;
        int pageNum = Integer.valueOf(pageData.get("page") + "");   //起始页
        int pageSize = Integer.valueOf(pageData.get("limit") + ""); //每页条数
        List<Deployment> list = repositoryService.createDeploymentQuery()
                .listPage(pageSize * (pageNum - 1), pageSize);
        long count = repositoryService.createDeploymentQuery().count();
        response = new BaseResponse(StatusCode.Success.getCode(),StatusCode.Success.getMsg(),count,list);
        return response;

    }
}
