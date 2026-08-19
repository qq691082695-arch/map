<script>
	export default {
		onLaunch: function() {
			console.log('App Launch')
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style>
	/*每个页面公共css */
	page {
		background: #f5f6f8;
		color: #1f2329;
		font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', 'PingFang SC',
			'Microsoft YaHei', sans-serif;
	}

	/* 按钮规范化：去除小程序默认灰底/边框，统一为圆角胶囊、内容居中 */
	button {
		margin: 0;
		padding: 0 30rpx;
		border: none;
		width: 100%;
		box-sizing: border-box;
		display: flex;
		align-items: center;
		justify-content: center;
		background: transparent;
		color: inherit;
		font-size: 28rpx;
		line-height: 1.4;
		min-height: 84rpx;
		border-radius: 42rpx;
		text-align: center;
	}
	button::after {
		border: none;
	}

	/* 通用按压反馈（配合 hover-class） */
	.btn-hover {
		opacity: 0.85;
	}
</style>
