<template>
	<view class="page">
		<cu-custom bgColor="bg-cyan" :isBack="true">
			<block slot="backText">返回</block>
			<block slot="content">骨料溯源</block>
		</cu-custom>

		<view class="hero-card shadow">
			<view class="hero-top">
				<view>
					<view class="hero-title">RFID骨料主体查询</view>
					<view class="hero-desc">适配PDA现场作业，先扫标签，再查主体</view>
				</view>
				<view class="hero-badge">扫码即查</view>
			</view>

			<view class="scan-panel">
				<view class="panel-label">骨料RFID编码</view>
				<view class="search-row">
					<view class="search-box">
						<text class="cuIcon-qr_code search-icon"></text>
						<text class="search-text">{{ form.rfidCode }}</text>
					</view>
					<button class="cu-btn bg-green lg round query-btn" @tap="queryTrace">搜索</button>
				</view>
			</view>

			<view class="hero-stats">
				<view class="stat-card">
					<view class="stat-label">当前状态</view>
					<view class="stat-value">{{ result.found ? result.bindStatus : '待查询' }}</view>
				</view>
				<view class="stat-card">
					<view class="stat-label">主体类型</view>
					<view class="stat-value">{{ result.found ? result.subjectType : '--' }}</view>
				</view>
				<view class="stat-card">
					<view class="stat-label">最近位置</view>
					<view class="stat-value small">{{ result.found ? result.latestLocation : '--' }}</view>
				</view>
			</view>
		</view>

		<view class="content-card shadow">
			<view class="content-head">
				<view>
					<view class="content-title">主体信息</view>
					<view class="content-subtitle">用于现场快速核验RFID与主体绑定关系</view>
				</view>
				<view class="state-pill" :class="result.found ? 'success' : 'idle'">
					{{ result.found ? '已匹配' : '待查询' }}
				</view>
			</view>

			<view v-if="result.found">
				<view class="subject-card">
					<view class="subject-top">
						<view>
							<view class="subject-name">{{ result.subjectName }}</view>
							<view class="subject-meta">{{ result.subjectCode }} | {{ result.subjectType }}</view>
						</view>
						<view class="bind-tag">{{ result.bindStatus }}</view>
					</view>

					<view class="subject-grid">
						<view class="grid-item">
							<view class="grid-label">骨料名称</view>
							<view class="grid-value">{{ result.boneName }}</view>
						</view>
						<view class="grid-item">
							<view class="grid-label">骨料批次</view>
							<view class="grid-value">{{ result.batchNo }}</view>
						</view>
						<view class="grid-item">
							<view class="grid-label">模板名称</view>
							<view class="grid-value">{{ result.templateName }}</view>
						</view>
						<view class="grid-item">
							<view class="grid-label">绑定时间</view>
							<view class="grid-value">{{ result.bindTime }}</view>
						</view>
						<view class="grid-item full">
							<view class="grid-label">当前位置</view>
							<view class="grid-value">{{ result.latestLocation }}</view>
						</view>
					</view>
				</view>

				<view class="detail-card">
					<view class="block-title">主体字段</view>
					<view class="field-cards">
						<view class="field-card" v-for="(item, index) in result.fields" :key="index">
							<view class="field-chip">{{ item.label }}</view>
							<view class="field-card-value">{{ item.value }}</view>
						</view>
					</view>
				</view>

				<view class="detail-card">
					<view class="block-title">最近流转</view>
					<view class="timeline-item" v-for="(item, index) in result.timeline" :key="index">
						<view class="timeline-dot"></view>
						<view class="timeline-body">
							<view class="timeline-top">
								<view class="timeline-title">{{ item.title }}</view>
								<view class="timeline-stage">{{ item.stage }}</view>
							</view>
							<view class="timeline-desc">{{ item.desc }}</view>
							<view class="timeline-footer">
								<view class="timeline-operator">{{ item.operator }}</view>
								<view class="timeline-time">{{ item.time }}</view>
							</view>
						</view>
					</view>
				</view>
			</view>

			<view v-else class="empty-box">
				<text class="cuIcon-search empty-icon"></text>
				<view class="empty-title">等待扫描后查询</view>
				<view class="empty-desc">建议操作流程：PDA扫描RFID -> 自动回填编码 -> 点击搜索 -> 核验主体。</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				form: {
					rfidCode: 'RFID-AGG-20260512-00018'
				},
				result: {
					found: false,
					subjectName: '',
					subjectCode: '',
					subjectType: '',
					bindStatus: '',
					boneName: '',
					batchNo: '',
					templateName: '',
					bindTime: '',
					latestLocation: '',
					fields: [],
					timeline: []
				}
			}
		},
		methods: {
			queryTrace() {
				if (!this.form.rfidCode) {
					this.mes('请先通过PDA扫描RFID编码')
					return
				}
				this.result = {
					found: true,
					subjectName: '梁板主体 A-01',
					subjectCode: 'SUBJECT-20260512-01',
					subjectType: '预制构件',
					bindStatus: '已绑定',
					boneName: '高强骨料预制件',
					batchNo: 'BATCH-20260510-03',
					templateName: '骨料预制品模板',
					bindTime: '2026-05-12 13:40',
					latestLocation: '1号库区 / 东侧堆场 / A通道',
					fields: [{
						label: '项目名称',
						value: '城西快速路改造项目'
					}, {
						label: '施工单位',
						value: '飞修建设集团'
					}, {
						label: '构件规格',
						value: 'T梁 30m'
					}, {
						label: '质量等级',
						value: '合格品'
					}, {
						label: '责任人',
						value: '王建国'
					}],
					timeline: [{
						title: '主体绑定完成',
						stage: '绑定',
						desc: 'RFID已绑定到梁板主体 A-01',
						operator: '操作人：王建国',
						time: '2026-05-12 13:40'
					}, {
						title: '入场验收',
						stage: '验收',
						desc: '完成预制件入场和身份校验',
						operator: '操作人：李建峰',
						time: '2026-05-12 11:15'
					}, {
						title: '骨料出厂',
						stage: '出厂',
						desc: '骨料从生产基地出厂，状态正常',
						operator: '操作人：赵东',
						time: '2026-05-11 18:22'
					}]
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.page {
		min-height: 100vh;
		padding: 24rpx;
		position: relative;
		background:
			linear-gradient(180deg, #1fca7c 0%, #83e0a8 32%, #eef9f1 52%, #f6f8fb 72%, #f4f6f8 100%);
	}

	.page::before {
		content: '';
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		height: 46vh;
		background:
			radial-gradient(circle at top right, rgba(255, 255, 255, 0.24), transparent 22%),
			radial-gradient(circle at left 20% top 8%, rgba(255, 255, 255, 0.18), transparent 18%);
		pointer-events: none;
	}

	.hero-card,
	.content-card {
		background: #ffffff;
		border-radius: 28rpx;
		padding: 28rpx;
		margin-bottom: 24rpx;
	}

	.hero-top,
	.content-head,
	.subject-top {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.hero-title,
	.content-title,
	.subject-name,
	.block-title {
		font-size: 34rpx;
		font-weight: 700;
		color: #1f2937;
	}

	.hero-desc,
	.content-subtitle,
	.subject-meta,
	.grid-label,
	.field-label,
	.timeline-desc,
	.timeline-time,
	.empty-desc,
	.tip-text {
		font-size: 24rpx;
		color: #6b7280;
	}

	.hero-badge,
	.state-pill,
	.bind-tag {
		padding: 10rpx 18rpx;
		border-radius: 999rpx;
		font-size: 22rpx;
		font-weight: 600;
	}

	.hero-badge {
		background: #e8f8ee;
		color: #14944f;
	}

	.state-pill.idle {
		background: #f3f4f6;
		color: #6b7280;
	}

	.state-pill.success,
	.bind-tag {
		background: #dcfce7;
		color: #15803d;
	}

	.scan-panel {
		margin-top: 28rpx;
	}

	.panel-label {
		font-size: 26rpx;
		font-weight: 600;
		color: #374151;
		margin-bottom: 16rpx;
	}

	.search-row {
		display: flex;
		align-items: center;
		margin-top: 12rpx;
	}

	.hero-stats {
		display: flex;
		margin-top: 24rpx;
		margin-left: -8rpx;
		margin-right: -8rpx;
	}

	.stat-card {
		flex: 1;
		margin: 0 8rpx;
		padding: 20rpx 18rpx;
		border-radius: 22rpx;
		background: linear-gradient(180deg, #f8fcf9 0%, #eef9f3 100%);
		border: 1rpx solid #daf2e3;
		min-height: 128rpx;
	}

	.stat-label {
		font-size: 22rpx;
		color: #6b7280;
	}

	.stat-value {
		margin-top: 14rpx;
		font-size: 30rpx;
		font-weight: 700;
		color: #14213d;
		line-height: 1.35;
	}

	.stat-value.small {
		font-size: 24rpx;
	}

	.search-box {
		flex: 1;
		height: 92rpx;
		border-radius: 999rpx;
		background: #f6faf7;
		border: 2rpx solid #cfeeda;
		display: flex;
		align-items: center;
		padding: 0 28rpx;
		box-shadow: inset 0 0 0 1rpx rgba(34, 197, 94, 0.04);
	}

	.search-icon {
		font-size: 34rpx;
		color: #19a95b;
		margin-right: 16rpx;
	}

	.search-text {
		flex: 1;
		font-size: 28rpx;
		font-weight: 600;
		color: #111827;
		word-break: break-all;
	}

	.query-btn {
		width: 188rpx;
		height: 92rpx;
		margin-left: 16rpx;
	}

	.subject-card,
	.detail-card {
		margin-top: 24rpx;
		background: #f9fbfb;
		border: 1rpx solid #edf2f7;
		border-radius: 24rpx;
		padding: 24rpx;
	}

	.subject-card {
		background: linear-gradient(180deg, #fbfffc 0%, #f7fbff 100%);
	}

	.subject-grid {
		display: flex;
		flex-wrap: wrap;
		margin-top: 22rpx;
	}

	.grid-item {
		width: 50%;
		margin-bottom: 20rpx;
	}

	.grid-item.full {
		width: 100%;
	}

	.grid-value,
	.timeline-title {
		font-size: 28rpx;
		font-weight: 600;
		color: #111827;
		margin-top: 8rpx;
	}

	.field-cards {
		display: flex;
		flex-wrap: wrap;
		margin-top: 20rpx;
	}

	.field-card {
		width: calc(50% - 10rpx);
		margin-bottom: 20rpx;
		margin-right: 20rpx;
		padding: 20rpx;
		border-radius: 20rpx;
		background: #ffffff;
		border: 1rpx solid #edf2f7;
		box-shadow: 0 6rpx 18rpx rgba(15, 23, 42, 0.04);
	}

	.field-card:nth-child(2n) {
		margin-right: 0;
	}

	.field-chip {
		display: inline-flex;
		align-items: center;
		padding: 8rpx 16rpx;
		border-radius: 999rpx;
		background: #ecfdf3;
		color: #15803d;
		font-size: 22rpx;
		font-weight: 600;
	}

	.field-card-value {
		margin-top: 16rpx;
		font-size: 28rpx;
		font-weight: 700;
		color: #111827;
		line-height: 1.5;
	}

	.timeline-item {
		display: flex;
		align-items: flex-start;
		padding-top: 22rpx;
	}

	.timeline-dot {
		width: 18rpx;
		height: 18rpx;
		border-radius: 50%;
		background: #22c55e;
		margin-top: 12rpx;
		margin-right: 18rpx;
		box-shadow: 0 0 0 8rpx rgba(34, 197, 94, 0.12);
	}

	.timeline-body {
		flex: 1;
		background: #ffffff;
		border-radius: 20rpx;
		padding: 22rpx;
		border: 1rpx solid #edf2f7;
		box-shadow: 0 6rpx 18rpx rgba(15, 23, 42, 0.04);
	}

	.timeline-top,
	.timeline-footer {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.timeline-stage {
		padding: 8rpx 16rpx;
		border-radius: 999rpx;
		font-size: 22rpx;
		font-weight: 600;
		color: #0f766e;
		background: #ccfbf1;
	}

	.timeline-footer {
		margin-top: 16rpx;
	}

	.timeline-operator {
		font-size: 24rpx;
		color: #4b5563;
	}

	.empty-box {
		padding: 80rpx 30rpx;
		text-align: center;
	}

	.empty-icon {
		display: block;
		font-size: 88rpx;
		color: #22c55e;
		margin-bottom: 24rpx;
	}

	.empty-title {
		font-size: 30rpx;
		font-weight: 700;
		color: #1f2937;
		margin-bottom: 16rpx;
	}
</style>
