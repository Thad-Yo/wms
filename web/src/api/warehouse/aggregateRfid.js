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

export function listAggregateRfidByMaterial(materialId) {
  return request({
    url: '/warehouse/aggregate/rfid/material/' + materialId,
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

export function bindAggregateRfidObject(data) {
  return request({
    url: '/warehouse/aggregate/rfid/bindObject',
    method: 'put',
    data: data
  })
}

export function exportAggregateRfidBindTemplate() {
  return request({
    url: '/warehouse/aggregate/rfid/exportBindTemplate',
    method: 'post',
    responseType: 'blob'
  })
}

export function listAggregateRfidObjectOptions(query) {
  return request({
    url: '/warehouse/aggregate/rfid/objectOptions',
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
