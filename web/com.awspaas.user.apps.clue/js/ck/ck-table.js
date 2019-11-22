/**
 * 基于iview table二次封装的表格组件
 * @author ck
 */
Vue.component('ck-table', {
    props: {
        url: {
            type: String,
            require: true
        },
        idField: {
            type: String
        },
        columns: {
            type: Array,
            default() {
              return [];
            }
        },
        showIndex: {
            type: Boolean,
            default: false
        },
        showSelection: {
            type: Boolean,
            default: false
        },
        showElevator: {
            type: Boolean,
            default: true
        },
        size: {
            type: String,
            default: 'small'
        },
        width: {
            type: [Number, String],
            default: 'auto'
        },
        height: {
            type: [Number, String]
        },
        stripe: {
            type: Boolean,
            default: false
        },
        border: {
            type: Boolean,
            default: false
        },
        showHeader: {
            type: Boolean,
            default: true
        },
        highlightRow: {
            type: Boolean,
            default: false
        },
        noDataText: {
            type: String
        },
        noFilteredDataText: {
            type: String
        },
        disabledHover: {
            type: Boolean
        },
        sizeTextFontSize: {
            type: String,
            default: '12px'
        },
        pagerPageSize: {
            type: Number,
            default: 50
        },
        pagerPageSizeOpts: {
            type: Array,
            default() {
              return [20, 50, 80, 100];
            }
        },
        indexWidth: {
            type: Number,
            default: 60
        },
        queryParams: {
            type: Object,
            default : {}
        },
        onLoadSuccess: {
            type: Function,
            default : function(){}
        },
        columnDefaultValue:{
            type: String
        },
        showPagination:{
            type: [Boolean, String],
            default : 'auto'
        }
    },
    template:` 
    <div>
        <div class="table-btn-group">
            <slot name="toolButtons"></slot>
        </div>
        <Table
            :data="data"
            :columns="columns"
            :stripe="stripe"
            :border="border"
            :show-header="showHeader"
            :width="width"
            :height="height"
            :loading="loading"
            :disabled-hover="disabledHover"
            :highlight-row="highlightRow"
            :row-class-name="rowClassName"
            :size="size"
            :no-data-text="noDataText"
            :no-filtered-data-text="noFilteredDataText"
            @on-current-change="onCurrentChange"
            @on-select="onSelect"
            @on-select-cancel="onSelectCancel"
            @on-select-all="onSelectAll"
            @on-selection-change="onSelectionChange"
            @on-sort-change="onSortChange"
            @on-filter-change="onFilterChange"
            @on-row-click="onRowClick"
            @on-row-dblclick="onRowDblclick"
            @on-expand="onExpand">
        </Table>
        <!-- 分页 -->
        <div v-if="showPage" style="background:#fff;padding: 8px;border-bottom: 1px solid #d6d2d2;border-left: 1px solid #d6d2d2;border-right: 1px solid #d6d2d2;" ref="pagerDiv">
            <Row>
                <Col span="8"><label style="line-height: 32px;padding-left:3.5%;color:#rgb(81, 90, 110);background:#fff;" :style="{fontSize: sizeTextFontSize}">{{sizeText}}</label></Col>
                <Col span="16">
                    <Page  
                        show-sizer 
                        :total="total" 
                        :current="pageNum" 
                        :page-size="pageSize"
                        :show-elevator="showElevator"
                        :page-size-opts="pagerPageSizeOpts" 
                        @on-change="changePage"
                        @on-page-size-change="changePageSize" />
                </Col>
            </Row>
        </div>
    </div>
    `,
    data(){
        return {
            pageNum: 1,
            pageSize: 50,
            sortField: '',
            sortType: '',
            total: 0,
            data: [],
            selectedRows: [],
            loading: false,
            showPage: false
        }
    },
    computed: {
        sizeText: function() {
            let end = Math.ceil(this.total/this.pageSize);
            end = end > this.total ? this.total : end;
            return `共${this.total}条记录，当前第${this.pageNum}/${end}页`;
        }
    },
    methods:{
        rowClassName(row) {
              if (this.showSelection) {
              if (row._checked) {
                return 'table-selected-row';
              }
            }
            return '';
        },
        onCurrentChange(currentRow, oldCurrentRow) {
            this.$emit('on-current-change', currentRow, oldCurrentRow);
        },
        onSelect(selection, row) {
            this.$emit('on-select', selection, row);
        },
        onSelectCancel(selection, row) {
            this.$emit('on-select-cancel', selection, row);
        },
        onSelectAll(selection) {
            this.$emit('on-select-all', selection);
        },
        // 点击多选框时触发事件
        onSelectionChange(selection) {
            this.data.forEach((item) => {
                let value = item[this.idField];
                let index = this.selectedRows.findIndex((element)=>(element[this.idField] == value));
                if(index != -1){
                    this.selectedRows.splice(index,1);
                }
            });

            if(selection.length){
                this.selectedRows = this.selectedRows.concat(selection);
            }
            this.$emit('on-selection-change', this.selection);
        },
        getSelections(){
            return this.selectedRows;
        },
        getSelectionsId(){
            let idList = [];
            this.selectedRows.forEach((row) => {
                idList.push(row[this.idField]); 
            });
            return idList;
        },					
        onSortChange(column) {
            this.sortField = column.key;
            this.sortType = column.order === 'normal' ? 'asc' : column.order;
            this.load();
        },
        onFilterChange(row) {
            this.$emit('on-filter-change', row);
        },
        onRowClick(row, index) {
            this.$emit('on-row-click', row, index);
        },
        onRowDblclick(row, index) {
            this.$emit('on-row-dblclick', row, index);
        },
        onExpand(row, status) {
            this.$emit('on-expand', row, status);
        },
        // 页码跳转
        changePage(pageIndex) {
            this.pageNum = pageIndex;
            this.load();  
        },
        // 修改每页显示的行数
        changePageSize(pageSize) {
            this.pageNum = 1;
            this.pageSize = pageSize;
            this.load();
        },
        init() {
            this.pageNum = 1;
            this.pageSize = this.pagerPageSize;
            this.sortField = '';
            this.sortType = '';
            this.selectedRows = [];
            this.load();
        },
        getData(){
            return {
                total: this.total,
                rows: this.data
            }
        },
        load() {
            this.loading = true;

            let queryParams = this.queryParams;
            for(let field in queryParams){
                if(queryParams[field] == '' ){
                    delete queryParams[field]
                }
            }

            axios({
                url: this.url,
                method: 'post',
                params: {
                    queryParams: queryParams,                    
					pageNum: this.pageNum,    
                    pageSize: this.pageSize,
                    orderClause: this.sortField ? this.sortField + ' ' + this.sortType : ''
                }
            }).then(res=>{
                this.total = res.data.result.total;
                
                if(this.total > 0){
                    this.data = res.data.result.rows;
                    //处理每列空置展示
                    if(this.columnDefaultValue){
                        this.columns.forEach((item) => {
                            let field = item.key;
                            if(field && field != this.idField){
                                this.data.forEach((row) => {
                                    let value = row[field];
                                    if(value == ''){
                                        row[field] = this.columnDefaultValue;
                                    }
                                });
                            }
                        });
                    }
                    
                    if(this.showPagination == 'auto'){
                        //查询结果小于当前每页显示的条数，不显示分页
                        this.showPage = this.total < this.pageSize ? false : true; 
                    }else{
                        this.showPage = this.showPagination;
                    } 
                    //选中项的挑√
                    if (this.showSelection) {
                        // 根据选中的数据来设置整个表格每行数据的_checked属性
                        this.data.forEach((item) => {
                            let value = item[this.idField];
                            let index = this.selectedRows.findIndex((element)=>(element[this.idField] == value));
                            if(index != -1){
                                item._checked = true;
                            }
                        });
                    }
                }

                this.onLoadSuccess();   
                this.loading = false;
            }).catch((error)=>{
                this.data = [];
                this.total = 0;
                this.$Message.warning('系统异常，请联系工作站！');
                this.loading = false;
            });
        },
        reload() {
            // this.$emit('load-data');
            this.load();
        }
    },
    created(){
        this.pageSize = this.pagerPageSize;
        if (this.showIndex) {
            this.columns.unshift({
              type: 'index',
              width: this.indexWidth,
              align: 'center',
              indexMethod: (row) => {
                return this.pageSize * (this.pageNum - 1) + row._index + 1;
              },
              // 原来是的序号表头会显示#，自定义成自己想要的文字显示
              renderHeader: (h) => {
                return h('span', '序号');
              }
            });
        }
        if (this.showSelection) {
            this.columns.unshift({
              width: 50, type: 'selection', align: 'center'
            });
        }
    }
});