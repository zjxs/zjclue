Vue.component('sy-table', {
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
                <Col span="8"><label style="line-height: 32px;padding-left:3.5%;color:#000;background:#fff;" :style="{fontSize: sizeTextFontSize}">{{sizeText}}</label></Col>
                <Col span="16">
                    <Page  
                        show-sizer 
                        :total="total" 
                        :current="currPage" 
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
            currPage: 1,
            pageSize: 50,
            sortField: '',
            sortType: '',
            total: 0,
            data: [],
            selectedRows: [],
            loading: false,
            showPage: false,
			isnototal:false,
        }
    },
    computed: {
        sizeText: function() {
            let end = Math.ceil(this.total/this.pageSize);
            end = end > this.total ? this.total : end;
            return `共${this.total}条记录，当前第${this.currPage}/${end}页`;
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
        	this.selectedRows = currentRow;
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
            if(this.highlightRow){
            	idList.push(this.selectedRows)
            }else{
            	this.selectedRows.forEach((row) => {
                	idList.push(row[this.idField]); 
            	});
            }
            console.log(idList);
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
            this.currPage = pageIndex;
            this.load();  
        },
        // 修改每页显示的行数
        changePageSize(pageSize) {
            this.currPage = 1;
            this.pageSize = pageSize;
            this.load();
        },
        init() {
            this.currPage = 1;
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
            let that = this;

            let queryParams = that.queryParams;
            for(const field in queryParams){
                if(queryParams[field] == '' ){
                    delete queryParams[field]
                }
            }
			let json= {
			  data: queryParams,
			  page: that.currPage,
			  rows: that.pageSize,
			  sortField: that.sortField,
			  sortType: that.sortType
			};
			let param = new URLSearchParams();
			param.append("json",JSON.stringify(json));
            axios({
                url: that.url,
                method: 'post',
                data: param
            }).then(response=>{
                that.total= response.data.data.total;
                that.data = response.data.data.rows;
				that.isnototal = that.data.length > 0 ? true : false;
                //查询结果小于当前每页显示的条数，不显示分页
                that.showPage = that.total < that.pageSize ? false : true;     
                //选中项的挑√
                if (that.selectedRows.length > 0) {
                    // 根据选中的数据来设置整个表格每行数据的_checked属性
                    that.data.forEach((item) => {
                        let value = item[that.idField];
                        let index = that.selectedRows.findIndex((element)=>(element[that.idField] == value));
                        if(index != -1){
                            item._checked = true;
                        }
                    });
                }
                that.onLoadSuccess();   
                that.loading = false;
            }).catch((error)=>{
                that.data = [];
                that.total = 0;
                that.$Message.warning('系统异常，请联系工作站！');
                that.loading = false;
                console.log(error);
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
                return this.pageSize * (this.currPage - 1) + row._index + 1;
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