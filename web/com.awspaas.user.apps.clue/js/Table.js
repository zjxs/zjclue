Vue.component('sy-table', {
    props: {
        url: {
            type: String,
            require: true
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
        context: {
            type: Object
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
            default: '14px'
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
        params: {
            type: Object
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
        <div v-if="showPage" style="padding: 8px;border-bottom: 1px solid #d6d2d2;border-left: 1px solid #d6d2d2;border-right: 1px solid #d6d2d2;" ref="pagerDiv">
            <Row>
                <Col span="18">
                    <Page 
                        style="float: left;"
                        show-sizer 
                        :total="total" 
                        :current="currPage" 
                        :page-size="pageSize"
                        :show-elevator="showElevator"
                        :page-size-opts="pagerPageSizeOpts" 
                        @on-change="changePage"
                        @on-page-size-change="changePageSize" />
                    <span style="display: inline-block;float: left;margin-left: 20px"><Button icon=" iconfont icon-shuaxin1" size="small" @click="reload"></Button></span>
                </Col>
                <Col span="6" style="text-align: right"><label :style="{fontSize: sizeTextFontSize}">{{sizeText}}</label></Col>
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
            // 已选择的数据行
            selects: [],
            selectRows:{},
            selectArr:[],
            loading: false,
            showPage: false
        }
    },
    computed: {
        sizeText: function() {
            let end = Math.ceil(this.total/this.pageSize);
            end = end > this.total ? this.total : end;
            return `共${this.total}条记录，当前第${this.currPage}/${end}页`;
        }
    },
    watch: {
        // propData: function (newList) {
        //   // 重置已选择的行数据
        //   this.selects = [];
        //   this.data = newList.concat();
        //   this.data.forEach((item) => {
        //     this.$set(item, '_checked', false);
        //   });
        // }
    },
    methods:{
        test() {
            console.log('test……');
        },
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
            this.removeHistoryRows();
            this.selects = selection;
            this.selectRows[this.currPage] = this.selects;
            for(let i = 0 ; i < this.selectRows[this.currPage].length ; i++ ){
                this.selectArr.push(this.selectRows[this.currPage][i]);
            }
            selection = this.selectArr.length == 0 ? selection : this.selectArr;
            if (this.showSelection) {
                this.data.forEach((item) => {
                // 根据选中的数据来设置整个表格每行数据的_checked属性
                let temp = selection.filter(item1 => item1.id == item.id);
                if (temp && temp.length > 0) {
                    item._checked = true;
                } else {
                    item._checked = false;
                }
                });
            } 
            this.$emit('on-selection-change', selection);
        },
        onSortChange(column) {
            this.sortField = column.key;
            this.sortType = column.order;
            this.search();
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
            if (this.currPage != pageIndex) {
                this.removeHistoryRows();
                let arr = [];
                for(let i = 0 ; i < this.selects.length ; i++ ){
                    arr.push(this.selects[i]);
                    this.selectRows[this.currPage] = arr;
                }
                if(this.selectRows[this.currPage] != undefined){
                    for(var i = 0 ; i < this.selectRows[this.currPage].length ; i++ ){
                        this.selectArr.push(this.selectRows[this.currPage][i]);
                    }
                }
                this.currPage = pageIndex;
                this.search();  
                if (this.showSelection) {
                this.data.forEach((item) => {
                    // 根据选中的数据来设置整个表格每行数据的_checked属性
                    let temp = this.selectArr.filter(item1 => item1.id == item.id);
                    if (temp && temp.length > 0) {
                        item._checked = true;
                    } else {
                        item._checked = false;
                    }
                });
                } 
            } else {
                console.log('这是由于每页显示大小改变触发的……');
            }
        },
        // 修改每页显示的行数
        changePageSize(pageSize) {
            this.currPage = 1;
            this.pageSize = pageSize;
            this.search();
        },
        getParams(){
            return {
                data: this.params,
                page: this.currPage,
                rows: this.pageSize,
                sortField: this.sortField,
                sortType: this.sortType
            }
        },
        search() {
            //父页面传过来的值
            let queryParams = this.getParams();
//			        if(event&&event.target==event.currentTarget);
            if(event.button!=undefined){
                //判断如果不是点击下方调整每页显示条数，就把数据还原成初始的值
                queryParams.rows=this.pagerPageSize;
                this.pageSize=this.pagerPageSize;
            }
            this.loading = true;
            var _this = this;
            setTimeout(function(){
                _this.total= 6; 
                _this.data= [
                    {
                        id: '001',
                        name: '张yi',
                        phone: '15678568930',
                        address: '北京理工大学'
                    },
                    {
                        id: '002',
                        name: '李er',
                        phone: '15678568930',
                        address: '北京科技大学'
                    },
                    {
                        id: '003',
                        name: '王san',
                        phone: '15678568930',
                        address: '清华大学'
                    },
                    {
                        id: '004',
                        name: '张si',
                        phone: '15678568930',
                        address: '北京理工大学'
                    },
                    {
                        id: '005',
                        name: '李wu',
                        phone: '15678568930',
                        address: '北京科技大学'
                    }
                ];
                _this.loading = false;
                //查询结果小于当前每页显示的条数，不显示分页
                _this.showPage = _this.total < _this.pageSize ? false : true;     
                //选中项的挑√
                if (_this.showSelection) {
                _this.data.forEach((item) => {
                    // 根据选中的数据来设置整个表格每行数据的_checked属性
                    let temp = _this.selectArr.filter(item1 => item1.id == item.id);
                    if (temp && temp.length > 0) {
                    item._checked = true;
                    } else {
                    item._checked = false;
                    }
                });
                } 
            },1000)
            //调用axios请求后台，注意异常的处理
        },
        getSelections() {
            return this.selects;
        },
        reload() {
            // this.$emit('load-data');
            this.search();
        },
        removeHistoryRows(){
            let historySelects = this.selectRows[this.currPage] ? this.selectRows[this.currPage] : []; 
                for (let i = historySelects.length - 1; i >= 0; i--) {
                let a = historySelects[i];
                for (let j = this.selectArr.length - 1; j >= 0; j--) {
                    let b = this.selectArr[j];
                    if (a == b) {
                        this.selectArr.splice(j, 1);
                        break;
                    }
                }
            }
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
})