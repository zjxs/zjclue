Vue.component('sy-tree', {
	//父组件传递过来的值（value<->v-model,tablename<->tablename）
	props: {
		value: {
			type: String,
		},
		tablename: {
			type: String,
		},
		placeholder: {
			type: String,
		},
		title: {
			type: String,
		},
		checkStrictly: {
			type: Boolean,
			default: false
		},
		//是否支持多选
		multiple: {
			type: Boolean,
			default: false
		},
		//是否显示多选框
		showCheckbox: {
			type: Boolean,
			default: false
		},
		//在显示复选框的情况下，是否严格的遵循父子不互相关联的做法
		checkStrictly: {
			type: Boolean,
			default: false
		},
		//开启后，在 show-checkbox 模式下，select 的交互也将转为 check
		checkDirectly: {
			type: Boolean,
			default: false
		},
		searchInput: { //默认查询条件
			type: String,
			default: ''
		}


	},
	//模板html
	template: ` ,
    <div>
        <Input v-model="inputText" :value="value" @on-click="openDialog()" :placeholder="placeholder" readonly icon="ios-card-outline"  />
        <Modal v-model="modal" :title="title" @on-ok="ok" @on-cancel="cancel">
			<Input v-model="searchInput" search enter-button placeholder="请输入" @on-search="search()" />
			<Tree style="margin-top: 15px;" :ref="tablename" :data="treedata" :load-data="loadData" :multiple="multiple" :show-checkbox="showCheckbox" :check-strictly="checkDirectly" :check-directly="checkDirectly" :check-strictly="checkStrictly"></Tree>
            <Spin size="large" fix v-if="spinShow">加载中...</Spin>
        </Modal>
    </div>
    `,
	data() {
		return {
			inputText: '',
			searchInput: '',
			modal: false,
			treedata: [],
			spinShow: false
		}
	},
	methods: {
		//点击树弹框的确认时的方法
		ok() {
			//获取选中的节点
			if (this.showCheckbox) {
				var list = this.$refs[this.tablename].getCheckedNodes();
			} else {
				var list = this.$refs[this.tablename].getSelectedNodes();
			}
			var names = [];
			var codes = [];
			for (var i = 0; i < list.length; i++) {
				names.push(list[i].title);
				codes.push(list[i].code);
			}
			//将选中节点的value值和code值填充到input框中
			this.inputText = names.join();
			var data = codes.join();
			// this.$parent.value=data;
			//将选中节点的value值传递到父组件            
			this.$emit('change', data);
		},
		cancel() {},
		openDialog: function() {
			this.searchInput = '';
			this.spinShow = true;
			this.modal = true;
			this.treedata = [];
			var json = {
				"table": this.tablename,
				"search": ""
			};
			if (this.searchInput == '') {
				axios({
					url: './jd',
					method: 'post',
					params: {
						'sid': sid,
						'json': json,
						cmd: 'com.awspaas.user.apps.clue.packageCode4Tree'
					}
				}).then(response => {
					this.treedata = response.data.data;
					this.spinShow = false;
				}).catch((error) => {
					console.log(error);
				});
			} else {
				console.log(this.searchInput);
				this.search();
			}

		},
		loadData(item, callback) {
			this.spinShow = true;
			var json = {
				"code": item.code,
				"table": this.tablename,
				"search": "",
				"lev": item.lev
			};
			axios({
				url: './jd',
				method: 'post',
				params: {
					'sid': sid,
					'json': json,
					cmd: 'com.awspaas.user.apps.clue.packageCode4Tree'
				}
			}).then(response => {
				this.spinShow = false;
				callback(response.data.data);
			}).catch((error) => {
				console.log(error);
			});

		},
		handleReset() { //表单重置清除内容方法调用
			this.inputText = '';
		},
		search() { //检索
			this.spinShow = true;
			var json = {
				"table": this.tablename,
				"search": this.searchInput,
			};
			axios({
				url: './jd',
				method: 'post',
				params: {
					'sid': sid,
					'json': json,
					cmd: 'com.awspaas.user.apps.clue.packageCode4Tree_select'
				}
			}).then(response => {
				this.spinShow = false;
				this.treedata = response.data.data;
				this.exchangeTree(true);
			}).catch((error) => {
				console.log(error);
			});
		},
		//展开或合并树，当flag为true时全部展开，flag为false时全部合并
		exchangeTree(flag) {
			this.taskTreeData = this.treeChangeExpand(this.treedata, flag);
		},
		treeChangeExpand(treeData, flag) {
			let _this = this;
			for (var i = 0; treeData && i < treeData.length; i++) {
				this.$set(treeData[i], 'expand', flag); //重要！用set方法
				if (treeData[i].children) {
					treeData[i].children = _this.treeChangeExpand(treeData[i].children, flag);
				}
			}
			return treeData;
		}




	},
});

function getParameters(url) {
	url = url || location.search;
	var params = {};
	var url = url.substr(url.indexOf('?') + 1);
	var paramsArr = url.split('&');
	for (var i = 0, len = paramsArr.length; i < len; i++) {
		var param = paramsArr[i];
		var split = param.split('=');
		params[split[0]] = split[1];
	}
	return params;
};

function getParameter(key, url) {
	return this.getParameters(url)[key];
};

function showLoadMask() {
	Vue.prototype.$Spin.show({
		render: (h) => {
			return h('div', [
				h('Icon', {
					'class': 'demo-spin-icon-load',
					props: {
						type: 'ios-loading',
						size: 18
					}
				}),
				h('div', '加载中')
			])
		}
	});
};

function hideLoadMask() {
	Vue.prototype.$Spin.hide();
};

function add0(m) {
	return m < 10 ? '0' + m : m
}

/**
 * 将时间戳转换为标准时间
 * 参数：shijianchuo为时间戳
 * 注意  参数shijianchuo为整数，否则需要parseInt转换
 */
function formatTonomal(shijianchuo) {
	var time = new Date(shijianchuo);
	var y = time.getFullYear();
	var m = time.getMonth() + 1;
	var d = time.getDate();
	var h = time.getHours();
	var mm = time.getMinutes();
	var s = time.getSeconds();
	return y + '-' + add0(m) + '-' + add0(d) + ' ' + add0(h) + ':' + add0(mm) + ':' + add0(s);
}
/**
 * 将UTC 通用标准时转为正常时间
 * 参数 time为UTC时间  format为时间格式
 */
function format(time, format) {
	var t = new Date(time);
	var tf = function(i) {
		return (i < 10 ? '0' : '') + i
	};
	return format.replace(/yyyy|MM|dd|HH|mm|ss/g, function(a) {
		switch (a) {
			case 'yyyy':
				return tf(t.getFullYear());
				break;
			case 'MM':
				return tf(t.getMonth() + 1);
				break;
			case 'mm':
				return tf(t.getMinutes());
				break;
			case 'dd':
				return tf(t.getDate());
				break;
			case 'HH':
				return tf(t.getHours());
				break;
			case 'ss':
				return tf(t.getSeconds());
				break;
		}
	})
}

function getUuid(len, radix) {
	var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
	var uuid = [], i;
	radix = radix || chars.length;
	if (len) {
		// Compact form
		for (i = 0; i < len; i++) uuid[i] = chars[0 | Math.random()*radix];
	} else {
		// rfc4122, version 4 form
		var r;
		// rfc4122 requires these characters
		uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
		uuid[14] = '4';
		// Fill in random data.  At i==19 set the high bits of clock sequence as
		// per rfc4122, sec. 4.1.5
		for (i = 0; i < 36; i++) {
			if (!uuid[i]) {
				r = 0 | Math.random()*16;
				uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
			}
		}
	}
	return uuid.join('');

}


/**
 * 去除特殊字符 （描述信息）
 */
function json_obj(str){
  var pattern= new RegExp("[\n,\t]") ; //创建一个包含\n的正则对象
  var result="";  //定义一个空字符
  for(var i=0;i<str.length;i++){
    result=result+str.substr(i,1).replace(pattern,'');//逐字检索 发现\n就换为空;
  } 
  return result; //返回转换完成的新json字符串
}

let utils = {

}
window.utils = utils;
