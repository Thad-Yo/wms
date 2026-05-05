import request from '@/utils/request'

export function listAggregateDevice(query) {
  return request({
    url: '/warehouse/aggregate/device/list',
    method: 'get',
    params: query
  })
}

export function getAggregateDevice(deviceId) {
  return request({
    url: '/warehouse/aggregate/device/' + deviceId,
    method: 'get'
  })
}

export function addAggregateDevice(data) {
  return request({
    url: '/warehouse/aggregate/device',
    method: 'post',
    data: data
  })
}

export function updateAggregateDevice(data) {
  return request({
    url: '/warehouse/aggregate/device',
    method: 'put',
    data: data
  })
}

export function delAggregateDevice(deviceId) {
  return request({
    url: '/warehouse/aggregate/device/' + deviceId,
    method: 'delete'
  })
}
