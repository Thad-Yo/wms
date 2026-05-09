import request from '@/utils/request'

export function listBoneObject(query) {
  return request({
    url: '/warehouse/aggregate/object/list',
    method: 'get',
    params: query
  })
}

export function getBoneObject(objectId) {
  return request({
    url: '/warehouse/aggregate/object/' + objectId,
    method: 'get'
  })
}

export function addBoneObject(data) {
  return request({
    url: '/warehouse/aggregate/object',
    method: 'post',
    data
  })
}

export function updateBoneObject(data) {
  return request({
    url: '/warehouse/aggregate/object',
    method: 'put',
    data
  })
}

export function bindBoneToObject(data) {
  return request({
    url: '/warehouse/aggregate/object/bindBone',
    method: 'put',
    data
  })
}

export function listBoneOptions(keyword) {
  return request({
    url: '/warehouse/aggregate/object/boneOptions',
    method: 'get',
    params: { keyword }
  })
}

export function syncBonePool() {
  return request({
    url: '/warehouse/aggregate/object/syncBonePool',
    method: 'post'
  })
}

export function getBoneObjectTimeline(objectId) {
  return request({
    url: '/warehouse/aggregate/object/timeline/' + objectId,
    method: 'get'
  })
}
