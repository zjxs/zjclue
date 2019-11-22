Vue.component('ck-tree', {
    //父组件传递过来的值（value<->v-model,tableName<->tableName）
    props: {
        tableName: {
            type: String,
            require: true
        },
        value: {
            type: String,
        },
        placeholder:{
            type: String
        },
        multiple:{
            type: Boolean,
            default: false
        },
        showCheckbox:{
            type: Boolean,
            default: false
        },
        emptyText:{
            type: String
        },
        checkStrictly:{
            type: Boolean,
            default: false
        },
        callback:{
            type: Function,
            default : function(){}
        },
        minLimit:{
            type:Number
        },
        maxLimit:{
            type:Number
        },
        whereClause:{
            type:String
        },
        orderClause:{
            type:String
        },
		title:{
			type:String
		}
    },
    //模板html
    template:` 
    <div>
        <Input 
            v-model="value" 
            :placeholder="placeholder" 
            :multiple="multiple"
             @on-click="openDialog()" 
             icon="md-open" 
             style="width: 200px"
             readonly 
        />
        <Modal id="treeModal" ref="treeModal" v-model="modal" :title="title" @on-ok="ok" width="500">
            <Input v-model="searchWord" search enter-button placeholder="请输入要查询的内容..." @on-search="onSearch"/>
            <div style="margin-top:10px;" v-if="showPreviewList">
                <Select 
                    v-model="previewValue"
                    clearable="true"
                    @on-change="onClearChange"
                    multiple>
                    <Option v-for="(option, index) in options" :value="option.value" :key="index">{{option.label}}</Option>
                </Select>
                </div>           
            <div style="margin-top:10px;max-height:300px;overflow-y:auto;">
            
                <Tree 
                    :ref="tableName" 
                    :data="data"
                    :load-data="loadData"
                    check-directly="true"
                    @on-select-change="onSelectChange"
                    @on-check-change="onCheckChange"
                    :show-checkbox="showCheckbox">
                </Tree>
                <Spin size="large" v-if="spinShow">加载中...</Spin>
            </div>
        </Modal>
    </div>
    `,
    data(){
        return {
            modal:false,
            data:[],
            spinShow:false,
            options:[],
            searchWord:'',
            showPreviewList:false,
            previewValue:[],
            checkedLength:0  
        }
    },
    methods:{
        openDialog:function(){
            this.searchWord = '';
            this.data = [];
            this.checkedLength = 0;
            this.showPreviewList = false;
            this.modal = true;
            this.onSearch();
        },
        //点击树弹框的确认时的方法
        ok(){
            //获取被勾选的节点
            let checkedNodes = this.$refs[this.tableName].getCheckedNodes();
            console.log(checkedNodes);

            let nameList = [] , codeList = [];
            checkedNodes.forEach((checkedNode) =>{
                nameList.push(checkedNode.title);
                codeList.push(checkedNode.code);
            });
 
            //将选中节点的value值和code值填充到input框中
            // this.value=codes.join();
            this.value = nameList.join();

            // this.$parent.value=data;
            //将选中节点的value值传递到父组件            
            this.$emit('change',codeList);
        },
        onSearch () {
            let otherParams = {
                searchWord : this.searchWord
            };
            this.getData(otherParams,result =>{
                this.data = result;
            });
        },
        getData (otherParams,callback){
            let defaultParams = {
                tableName : this.tableName,
                whereClause: this.whereClause,
                orderClause: this.orderClause
            };
            this.spinShow = true;
            axios({
                url:'./jd',
                method: 'post',
                //params:{'sid':sid,'json':json,cmd:'com.awspaas.user.apps.clue.packageCode4Tree'}
                params: Object.assign({'sid':sid,'cmd':'com.awspaas.user.apps.clue.packageCode4Tree_new'},defaultParams,otherParams)
            }).then(res=>{
                this.spinShow = false;
                if(res.data.result == 'ok'){
                    callback.call(null,res.data.data);
                }
            }).catch((error)=>{
                console.log(error);
            });
        },
        loadData (item, callback) {
            let otherParams = {
                parentLev: item.lev,
                parentCode: item.code
            };
            this.getData(otherParams,function(result){
                callback(result);
            });
        },
        onCheckChange (checkedNodes,checkedNode){           
            let index = this.options.findIndex((option)=>(option.value == checkedNode.code));
            if(index == -1){    //选中
                this.showPreviewList = true;
                this.options.push({
                    label: checkedNode.name,
                    value: checkedNode.code
                });
                this.previewValue.push(checkedNode.code);
            }else{  //移除
                if(checkedNodes.length == 0){
                    this.showPreviewList = false;
                }
                this.options.splice(index,1);
                this.previewValue.splice(index,1);
                
            }
            this.checkedLength = checkedNodes.length;  
        },
        onSelectChange(){
            
        },
        onClearChange(selectedValues){
            //TODO 触发取消掉的值的点击事件
            if(this.checkedLength > selectedValues.length){
                // let nodeElements = [];
                // let tags = document.getElementById('treeModal').getElementsByTagName('span');
                // for(let i=0;i<tags.length;i++){
                //    let tag = tags[i];
                //    if(tag.className == 'ivu-tree-title'){
                //        nodeElements.push(tag);
                //    }
                // }
            }
            
        },
        validate(){
            //TODO 未完成，心情好再改
            if(this.minLimit){

            }
            if(this.maxLimit){

            }
        }
    },
});