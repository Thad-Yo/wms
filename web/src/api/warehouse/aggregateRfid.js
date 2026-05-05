import request from '@/utils/request'

export function listAggregateRfid(query) {
  return request({
    url: '/warehouse/aggregate/rfid/list',
    method: 'get',
    params: query
  })
}

export function getAggregateRfid(identityId) {
  return request({
    url: '/warehouse/aggregate/rfid/' + identityId,
    method: 'get'
  })
}

export function addAggregateRfid(data) {
  return request({
    url: '/warehouse/aggregate/rfid',
    method: 'post',
    data: data
  })
}

export function updateAggregateRfid(data) {
  return request({
    url: '/warehouse/aggregate/rfid',
    method: 'put',
    data: data
  })
}

export function bindAggregateRfidGoods(data) {
  return request({
    url: '/warehouse/aggregate/rfid/bindGoods',
    method: 'put',
    data: data
  })
}

export function listAggregateRfidGoodsOptions(query) {
  return request({
    url: '/warehouse/aggregate/rfid/goodsOptions',
    method: 'get',
    params: query
  })
}

export function delAggregateRfid(identityId) {
  return request({
    url: '/warehouse/aggregate/rfid/' + identityId,
    method: 'delete'
  })
}

export function getAggregateLifecycle(rfidCode) {
  return request({
    url: '/warehouse/aggregate/rfid/lifecycle/' + rfidCode,
    method: 'get'
  })
}
