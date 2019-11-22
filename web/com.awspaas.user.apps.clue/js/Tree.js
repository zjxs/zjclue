Vue.component('my-tree', {
    //父组件传递过来的值（value<->v-model,tablename<->tablename）
    props: ['value','tablename'],
    //模板html
    template:` 
    <div>
        <Input v-model="inputText" :value="value" @on-click="openDialog()" icon="ios-clock-outline" style="width: 200px" />
        <Modal v-model="modal" title="title" @on-ok="ok" @on-cancel="cancel">
            <Tree :ref="tablename" :data="treedata" :load-data="loadData" show-checkbox></Tree>
        </Modal>
    </div>
    `,
    data(){
        return {
            inputText:'',
            modal:false,
            treedata:[]
        }
    },
    methods:{
        //点击树弹框的确认时的方法
        ok(){
            //获取选中的节点
            var list = this.$refs[this.tablename].getCheckedNodes();
            var names=[];
            var codes=[];
            for(var i = 0; i < list.length; i++) {
                names.push(list[i].title);
                codes.push(list[i].code);
            }
            //将选中节点的value值和code值填充到input框中
            // this.value=codes.join();
            this.inputText=names.join();
            var data=codes.join();
            // this.$parent.value=data;
            //将选中节点的value值传递到父组件            
            this.$emit('change',data);
        },
        cancel () {},
        openDialog:function(){
            showLoadMask();
            this.modal = true;
            this.treedata=[];
            var json={
                "table":this.tablename,
                "search":""
            };
            axios({
                url:'./jd',
                method: 'post',
                params:{'sid':sid,'json':json,cmd:'com.awspaas.user.apps.clue.packageCode4Tree'}
            }).then(response=>{
                this.treedata=response.data.data;
                hideLoadMask();
            }).catch((error)=>{
                console.log(error);
            });
        },
        loadData (item, callback) {
            showLoadMask(); 
            var json={
                "code":item.code,
                "table":this.tablename,
                "search":"",
                "lev": item.lev
            };
            axios({
                url:'./jd',
                method: 'post',
                params:{'sid':sid,'json':json,cmd:'com.awspaas.user.apps.clue.packageCode4Tree'}
            }).then(response=>{
               callback(response.data.data);
                hideLoadMask();
            }).catch((error)=>{
                console.log(error);
            });

      }
    },
});