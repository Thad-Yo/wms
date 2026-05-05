import request from '@/utils/request'

export function listAggregateMaterial(query) {
  return request({
    url: '/warehouse/aggregate/material/list',
    method: 'get',
    params: query
  })
}

export function getAggregateMaterial(materialId) {
  return request({
    url: '/warehouse/aggregate/material/' + materialId,
    method: 'get'
  })
}

export function addAggregateMaterial(data) {
  return request({
    url: '/warehouse/aggregate/material',
    method: 'post',
    data: data
  })
}

export function importAggregateMaterialBatch(data) {
  return request({
    url: '/warehouse/aggregate/material/importBatch',
    method: 'post',
    data: data
  })
}

export function updateAggregateMaterial(data) {
  return request({
    url: '/warehouse/aggregate/material',
    method: 'put',
    data: data
  })
}

export function delAggregateMaterial(materialId) {
  return request({
    url: '/warehouse/aggregate/material/' + materialId,
    method: 'delete'
  })
}
