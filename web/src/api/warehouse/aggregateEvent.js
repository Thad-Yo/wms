import request from '@/utils/request'

export function listAggregateEvent(query) {
  return request({
    url: '/warehouse/aggregate/event/list',
    method: 'get',
    params: query
  })
}

export function addAggregateEvent(data) {
  return request({
    url: '/warehouse/aggregate/event',
    method: 'post',
    data: data
  })
}
